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
package org.cleartk.classifier.opennlp;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.outcome.StringToStringOutcomeEncoder;
import org.cleartk.classifier.jar.JarDataWriterFactory;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren, Philipp Wetzler
 * 
 */

public class DefaultMaxentDataWriterFactory extends JarDataWriterFactory<List<NameNumber>, String, String> {

	/**
	 * "org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory.PARAM_COMPRESS"
	 * is a single, optional, boolean parameter, defaulting to false, that when true
	 * indicates that the FeaturesEncoder should compress the feature names.
	 * @see NameNumberFeaturesEncoder
	 */
	public static final String PARAM_COMPRESS = "org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory.PARAM_COMPRESS";

	/**
	 * "org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory.PARAM_SORT_NAME_LOOKUP"
	 * is a single, optional, boolean parameter, defaulting to false, that when true
	 * indicates that the FeaturesEncoder should write the feature names in sorted order.
	 * @see NameNumberFeaturesEncoder
	 */
	public static final String PARAM_SORT_NAME_LOOKUP = "org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory.PARAM_SORT_NAME_LOOKUP";

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		compress = (Boolean)UIMAUtil.getDefaultingConfigParameterValue(context, PARAM_COMPRESS, false);		
		sort = (Boolean)UIMAUtil.getDefaultingConfigParameterValue(context, PARAM_SORT_NAME_LOOKUP, false);
	}
	
	public DataWriter<String> createDataWriter() throws IOException {
		MaxentDataWriter mdw = new MaxentDataWriter(outputDirectory);
		
		if(!this.setEncodersFromFileSystem(mdw)) {
			NameNumberFeaturesEncoder featuresEncoder = new NameNumberFeaturesEncoder(compress, sort);
			featuresEncoder.addEncoder(new NumberEncoder());
			featuresEncoder.addEncoder(new BooleanEncoder());
			featuresEncoder.addEncoder(new StringEncoder());
			mdw.setFeaturesEncoder(featuresEncoder);
			
			mdw.setOutcomeEncoder(new StringToStringOutcomeEncoder());
		}
		
		return mdw;
	}

	private boolean compress;
	private boolean sort;
}
