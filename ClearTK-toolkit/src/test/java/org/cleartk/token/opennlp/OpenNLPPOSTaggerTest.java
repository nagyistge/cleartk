 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.token.opennlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TokenFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class OpenNLPPOSTaggerTest {
	
	@Test
	public void testSimple() throws UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				OpenNLPPOSTagger.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription(Token.class, Sentence.class),
				OpenNLPPOSTagger.PARAM_CASE_SENSITIVE, true,
				OpenNLPPOSTagger.PARAM_POSTAG_MODEL_FILE,
				"resources/models/OpenNLP.POSTags.English.bin.gz",
				OpenNLPPOSTagger.PARAM_POSTAG_DICTIONARY_FILE,
				"resources/models/OpenNLP.TagDict.txt");
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"The brown fox jumped quickly over the lazy dog.",
				Token.class, Sentence.class, 
				"The brown fox jumped quickly over the lazy dog .");
		engine.process(jCas);
		
		List<String> expected = Arrays.asList(
				"DT JJ NN VBD RB IN DT JJ NN .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token: AnnotationRetrieval.getAnnotations(jCas, Token.class)) {
			actual.add(token.getPos());
		}
		Assert.assertEquals(expected, actual);
		
	}

	@Test
	public void testDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.token.opennlp.OpenNLPPOSTagger",
				OpenNLPPOSTagger.PARAM_CASE_SENSITIVE, true,
				OpenNLPPOSTagger.PARAM_POSTAG_MODEL_FILE,
				"resources/models/OpenNLP.POSTags.English.bin.gz",
				OpenNLPPOSTagger.PARAM_POSTAG_DICTIONARY_FILE,
				"resources/models/OpenNLP.TagDict.txt");
		
		Object modelFile = engine.getConfigParameterValue(
				OpenNLPPOSTagger.PARAM_POSTAG_MODEL_FILE);
		Assert.assertEquals("resources/models/OpenNLP.POSTags.English.bin.gz", modelFile);
		
		Object dictionaryFile = engine.getConfigParameterValue(
				OpenNLPPOSTagger.PARAM_POSTAG_DICTIONARY_FILE);
		Assert.assertEquals("resources/models/OpenNLP.TagDict.txt", dictionaryFile);
		
		Object caseSensitive = engine.getConfigParameterValue(
				OpenNLPPOSTagger.PARAM_CASE_SENSITIVE);
		Assert.assertEquals(true, caseSensitive);
		
		engine.collectionProcessComplete();
	}
}
