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
package org.cleartk.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.uimafit.component.JCasAnnotatorAdapter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.JCasFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.JCasIterable;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public abstract class CleartkTestBase {

  protected JCas jCas;

  protected TypeSystemDescription typeSystemDescription;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  protected File outputDirectory;

  protected String outputDirectoryName;

  @Before
  public void setUp() throws Exception {
    typeSystemDescription = TypeSystemDescriptionFactory
        .createTypeSystemDescription(getTypeSystemDescriptorNames());
    jCas = JCasFactory.createJCas(typeSystemDescription);
    outputDirectory = folder.newFolder("output");
    outputDirectoryName = outputDirectory.getPath();
  }

  public abstract String[] getTypeSystemDescriptorNames();

  public int getCollectionReaderCount(CollectionReader reader) throws UIMAException, IOException {

    AnalysisEngine aeAdapter = AnalysisEngineFactory.createPrimitive(
        JCasAnnotatorAdapter.class,
        typeSystemDescription);

    int count = 0;
    JCasIterable jCases = new JCasIterable(reader, aeAdapter);
    for (@SuppressWarnings("unused")
    JCas jcs : jCases) {
      count++;
    }
    return count;
  }

  public void testCollectionReaderCount(CollectionReader reader, int expectedCount)
      throws UIMAException, IOException {
    assertEquals(expectedCount, getCollectionReaderCount(reader));
  }

}
