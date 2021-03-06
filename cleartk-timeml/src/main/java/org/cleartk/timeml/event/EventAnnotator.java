/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.timeml.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.liblinear.LibLinearStringOutcomeDataWriter;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Annotator for TimeML EVENT identification.
 * 
 * @author Steven Bethard
 */
public class EventAnnotator extends CleartkAnnotator<String> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {

    @Override
    public Class<?> getAnnotatorClass() {
      return EventAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterClass() {
      return LibLinearStringOutcomeDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createEngineDescription(EventAnnotator.class);
    }
  };

  protected List<FeatureExtractor1<Token>> tokenFeatureExtractors;

  protected List<CleartkExtractor<Token, Token>> contextExtractors;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // add features: word, stem, pos
    this.tokenFeatureExtractors = Lists.newArrayList();
    this.tokenFeatureExtractors.add(new CoveredTextExtractor<Token>());
    this.tokenFeatureExtractors.add(new TypePathExtractor<Token>(Token.class, "stem"));
    this.tokenFeatureExtractors.add(new TypePathExtractor<Token>(Token.class, "pos"));
    this.tokenFeatureExtractors.add(new ParentNodeFeaturesExtractor());

    // add window of features before and after
    this.contextExtractors = Lists.newArrayList();
    this.contextExtractors.add(new CleartkExtractor<Token, Token>(
        Token.class,
        new CoveredTextExtractor<Token>(),
        new Preceding(3),
        new Following(3)));
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Set<Token> eventTokens = new HashSet<Token>();
    if (this.isTraining()) {
      for (Event event : JCasUtil.select(jCas, Event.class)) {
        for (Token token : JCasUtil.selectCovered(jCas, Token.class, event)) {
          eventTokens.add(token);
        }
      }
    }

    int index = 1;
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence)) {
        List<Feature> features = new ArrayList<Feature>();
        for (FeatureExtractor1<Token> extractor : this.tokenFeatureExtractors) {
          features.addAll(extractor.extract(jCas, token));
        }
        for (CleartkExtractor<Token, Token> extractor : this.contextExtractors) {
          features.addAll(extractor.extractWithin(jCas, token, sentence));
        }
        if (this.isTraining()) {
          String label = eventTokens.contains(token) ? "Event" : "O";
          this.dataWriter.write(new Instance<String>(label, features));
        } else {
          if (this.classifier.classify(features).equals("Event")) {
            Event event = new Event(jCas, token.getBegin(), token.getEnd());
            event.setId(String.format("e%d", index));
            event.setEventInstanceID(String.format("ei%d", index));
            event.addToIndexes();
            index += 1;
          }
        }
      }
    }
  }

  private static class ParentNodeFeaturesExtractor implements FeatureExtractor1<Token> {
    public ParentNodeFeaturesExtractor() {
    }

    @Override
    public List<Feature> extract(JCas view, Token token)
        throws CleartkExtractorException {
      TreebankNode node = TreebankNodeUtil.selectMatchingLeaf(view, token);
      List<Feature> features = new ArrayList<Feature>();
      if (node != null) {
        TreebankNode parent = node.getParent();
        if (parent != null) {
          features.add(new Feature("ParentNodeType", parent.getNodeType()));
          TreebankNode firstSibling = parent.getChildren(0);
          if (firstSibling != node && firstSibling.getLeaf()) {
            features.add(new Feature("FirstSiblingText", firstSibling.getCoveredText()));
          }
        }
      }
      return features;
    }
  }
}
