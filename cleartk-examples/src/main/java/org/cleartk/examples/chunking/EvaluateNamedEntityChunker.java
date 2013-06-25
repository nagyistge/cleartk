/** 
 * Copyright (c) 2007-2012, Regents of the University of Colorado 
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
package org.cleartk.examples.chunking;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.mallet.MalletCRFStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.examples.chunking.TrainNamedEntityChunker.MASCTextFileFilter;
import org.cleartk.examples.chunking.util.MASCGoldAnnotator;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;

/**
 * <p>
 * This class can be used to train and test the named entity chunker. It can be used to either
 * perform 2-fold cross-validation, or training and testing on a holdout test set, or just to
 * evaluate/test a pre-trained model.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Himanshu Gahlot
 * @author Steven Bethard
 * 
 * 
 */
public class EvaluateNamedEntityChunker extends
    Evaluation_ImplBase<File, AnnotationStatistics<String>> {

  public static class Options extends Options_ImplBase {
    @Option(
        name = "--train-dir",
        usage = "Specify the directory containing the training documents.  This is used for cross-validation and for training in a holdout set evaluator. "
            + "When we run this example we point to a directory containing training data from the MASC-1.0.3 corpus - i.e. a directory called 'MASC-1.0.3/data/written'")
    public File trainDirectory = new File("data/MASC-1.0.3/data/written");

    @Option(
        name = "--models-dir",
        usage = "specify the directory in which to write out the trained model files")
    public File modelsDirectory = new File("target/chunking/ne-model");
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.parseOptions(args);

    // find training files
    List<File> trainFiles = new ArrayList<File>(FileUtils.listFiles(
        options.trainDirectory,
        new MASCTextFileFilter(),
        FileFilterUtils.falseFileFilter()));

    // run cross validation
    EvaluateNamedEntityChunker evaluator = new EvaluateNamedEntityChunker(options.modelsDirectory);
    List<AnnotationStatistics<String>> foldStats = evaluator.crossValidation(trainFiles, 2);
    AnnotationStatistics<String> crossValidationStats = AnnotationStatistics.addAll(foldStats);

    System.err.println("Cross Validation Results:");
    System.err.print(crossValidationStats);
    System.err.println();
    System.err.println(crossValidationStats.confusions());
    System.err.println();

    // train and save a model using all the data
    evaluator.trainAndTest(trainFiles, Collections.<File> emptyList());
  }

  public EvaluateNamedEntityChunker(File baseDirectory) {
    super(baseDirectory);
  }

  @Override
  protected CollectionReader getCollectionReader(List<File> files) throws Exception {
    return CollectionReaderFactory.createCollectionReader(UriCollectionReader.getDescriptionFromFiles(files));
  }

  @Override
  public void train(CollectionReader collectionReader, File outputDirectory) throws Exception {
    // assemble the training pipeline
    AggregateBuilder aggregate = new AggregateBuilder();

    // an annotator that loads the text from the training file URIs
    aggregate.add(UriToDocumentTextAnnotator.getDescription());

    // an annotator that parses and loads MASC named entity annotations (and tokens)
    aggregate.add(MASCGoldAnnotator.getDescription());

    // an annotator that adds part-of-speech tags
    aggregate.add(PosTaggerAnnotator.getDescription());

    // our NamedEntityChunker annotator, configured to write Mallet CRF training data
    aggregate.add(AnalysisEngineFactory.createPrimitiveDescription(
        NamedEntityChunker.class,
        CleartkSequenceAnnotator.PARAM_IS_TRAINING,
        true,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory,
        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MalletCRFStringOutcomeDataWriter.class));

    // run the pipeline over the training corpus
    SimplePipeline.runPipeline(collectionReader, aggregate.createAggregateDescription());

    // quiet Mallet down a bit (but still leave likelihoods so you can see progress)
    Logger malletLogger = Logger.getLogger("cc.mallet");
    malletLogger.setLevel(Level.WARNING);
    Logger likelihoodLogger = Logger.getLogger("cc.mallet.fst.CRFOptimizableByLabelLikelihood");
    likelihoodLogger.setLevel(Level.INFO);

    // train a Mallet CRF model on the training data
    Train.main(outputDirectory);

  }

  @Override
  protected AnnotationStatistics<String> test(CollectionReader collectionReader, File modelDirectory)
      throws Exception {

    final String defaultViewName = CAS.NAME_DEFAULT_SOFA;
    final String goldViewName = "GoldView";

    // define the pipeline
    AggregateBuilder aggregate = new AggregateBuilder();

    // Annotators processing the gold view:
    // * create the gold view
    // * load the text
    // * load the MASC annotations
    aggregate.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        goldViewName));
    aggregate.add(UriToDocumentTextAnnotator.getDescription(), defaultViewName, goldViewName);
    aggregate.add(MASCGoldAnnotator.getDescription(), defaultViewName, goldViewName);

    // Annotators processing the default (system) view:
    // * load the text
    // * parse sentences, tokens, part-of-speech tags
    // * run the named entity chunker
    aggregate.add(UriToDocumentTextAnnotator.getDescription());
    aggregate.add(SentenceAnnotator.getDescription());
    aggregate.add(TokenAnnotator.getDescription());
    aggregate.add(PosTaggerAnnotator.getDescription());
    aggregate.add(AnalysisEngineFactory.createPrimitiveDescription(
        NamedEntityChunker.class,
        CleartkSequenceAnnotator.PARAM_IS_TRAINING,
        false,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        JarClassifierBuilder.getModelJarFile(modelDirectory)));

    // prepare the evaluation statistics
    AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
    Function<NamedEntityMention, ?> getSpan = AnnotationStatistics.annotationToSpan();
    Function<NamedEntityMention, String> getCategory = AnnotationStatistics.annotationToFeatureValue("mentionType");

    // iterate over each JCas to be evaluated
    for (JCas jCas : new JCasIterable(collectionReader, aggregate.createAggregate())) {
      JCas goldView = jCas.getView(goldViewName);
      JCas systemView = jCas.getView(defaultViewName);

      // extract the named entity mentions from both gold and system views
      Collection<NamedEntityMention> goldMentions, systemMentions;
      goldMentions = JCasUtil.select(goldView, NamedEntityMention.class);
      systemMentions = JCasUtil.select(systemView, NamedEntityMention.class);

      // compare the system mentions to the gold mentions
      stats.add(goldMentions, systemMentions, getSpan, getCategory);
    }

    return stats;
  }
}
