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

package org.cleartk.corpus.ace2005;

import java.io.File;

import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.TearDownUtil;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 *
 */

public class Ace2005GoldTest{

	private final File rootDir = new File("test/data/corpus/ace2005");

	@Before
	public void setUp() {
		if(!rootDir.exists())
			rootDir.mkdirs();
	}
	
	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(rootDir);
		Assert.assertFalse(rootDir.exists());
	}
	
	@Test
	public void testReaderInvalidParameters() throws Exception {
		try {
			CollectionReaderFactory.createCollectionReader(
					Ace2005GoldReader.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
			Assert.fail("expected error for invalid corpus directory");
		}
		catch (ResourceInitializationException e) {
		}

		try {
			CollectionReaderFactory.createCollectionReader(
					Ace2005GoldReader.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					Ace2005GoldReader.PARAM_ACE_DIRECTORY_NAME, "foo/bar");
			Assert.fail("expected error for invalid corpus directory");
		}
		catch (ResourceInitializationException e) {
		}
	}
}
