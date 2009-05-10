/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.example.documentclassification;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.cleartk.ClearTKComponents;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.tfidf.IDFMapWriter;
import org.cleartk.util.UIMAUtil;
import org.uutuc.factory.AnalysisEngineFactory;


/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 *
 */

public class BuildIDFMap {

	public static void main(String[] args) throws UIMAException, IOException {
		String trainingDataDirectory;
		if(args.length <= 0) {
			trainingDataDirectory="../ClearTK Data/data/20newsgroups/20news-bydate-train";
		} else {
			trainingDataDirectory= args[0];
		}

		UIMAUtil.runUIMAPipeline(
				ClearTKComponents.createFilesCollectionReader(trainingDataDirectory),
				ClearTKComponents.createOpenNLPSentenceSegmenter(),
				ClearTKComponents.createTokenAnnotator(),
				ClearTKComponents.createSnowballStemmer("English"),
				AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(IDFMapWriter.class,
						ClearTKComponents.TYPE_SYSTEM_DESCRIPTION, ClearTKComponents.TYPE_PRIORITIES,
						IDFMapWriter.PARAM_IDFMAP_FILE, "example/documentclassification/idfmap",
						InstanceConsumer.PARAM_ANNOTATION_HANDLER, AnnotationHandler.class.getName()));
	}
}