/** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
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
package org.cleartk.feature.syntax;

import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2007-2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 */
public class SiblingExtractor implements FeatureExtractor1<TreebankNode> {

  public SiblingExtractor(int offset, FeatureExtractor1<TreebankNode> subExtractor) {
    this.offset = offset;
    this.subExtractor = subExtractor;

    if (offset < 0) {
      if (Math.abs(offset) > 1)
        this.name = String.format("%dLeftSibling");
      else
        this.name = "LeftSibling";
    } else if (offset > 0) {
      if (Math.abs(offset) > 1)
        this.name = String.format("%dRightSibling");
      else
        this.name = "RightSibling";
    } else {
      this.name = "";
    }
  }


  public List<Feature> extract(JCas jCas, TreebankNode node)
      throws CleartkExtractorException {
    TreebankNode parent = node.getParent();

    if (parent == null)
      return Collections.emptyList();

    List<TreebankNode> children = Lists.newArrayList(JCasUtil.select(
        parent.getChildren(),
        TreebankNode.class));
    int index = children.indexOf(node);
    int siblingIndex = index + offset;

    if (siblingIndex < 0 || siblingIndex >= children.size())
      return Collections.emptyList();

    TreebankNode sibling = children.get(siblingIndex);

    List<Feature> features = subExtractor.extract(jCas, sibling);
    for (Feature feature : features) {
      feature.setName(Feature.createName(name, feature.getName()));
    }

    return features;
  }

  private int offset;

  private String name;

  private FeatureExtractor1<TreebankNode> subExtractor;

}
