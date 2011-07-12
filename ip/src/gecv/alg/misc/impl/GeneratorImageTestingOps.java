/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.misc.impl;

import gecv.misc.AutoTypeImage;
import gecv.misc.CodeGeneratorUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * Generates functions inside of {@link gecv.alg.misc.ImageTestingOps}.
 *
 * @author Peter Abeles
 */
public class GeneratorImageTestingOps {

	String className = "ImageTestingOps";

	PrintStream out;

	private AutoTypeImage imageType;
	private String imageName;
	private String dataType;
	private String bitWise;

	public GeneratorImageTestingOps() throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(className + ".java"));
	}

	public void generate() {
		printPreamble();
		printAllGeneric();
		printAllSpecific();
		out.println("}");
	}

	private void printPreamble() {
		out.print(CodeGeneratorUtil.copyright);
		out.print("package gecv.alg.misc;\n" +
				"\n" +
				"import gecv.struct.image.*;\n" +
				"\n" +
				"import java.util.Random;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Image operations which are primarily used for testing and evaluation.\n" +
				" *\n" +
				" * DO NOT MODIFY: Generated by {@link gecv.alg.misc.impl.GeneratorImageTestingOps}.\n"+
				" *\n"+
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	public void printAllGeneric() {
		AutoTypeImage types[] = AutoTypeImage.getGenericTypes();

		for( AutoTypeImage t : types ) {
			imageType = t;
			imageName = t.getImageName();
			dataType = t.getDataType();
			printFill();
			printFillRectangle();
			printRandomize();
		}
	}

	public void printAllSpecific() {
		AutoTypeImage types[] = AutoTypeImage.getSpecificTypes();

		for( AutoTypeImage t : types ) {
			imageType = t;
			imageName = t.getImageName();
			dataType = t.getDataType();
			bitWise = t.getBitWise();
			printAddUniform();
		}
	}

	public void printFill()
	{
		String typeCast = imageType.getTypeCastFromSum();
		out.print("/**\n" +
				"\t * Fills the whole image with the specified pixel value\n" +
				"\t *\n" +
				"\t * @param img   An image.\n" +
				"\t * @param value The value that the image is being filled with.\n" +
				"\t */\n" +
				"\tpublic static void fill("+imageName+" img, "+imageType.getSumType()+" value) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n" +
				"\t\t\t\tdata[index++] = "+typeCast+"value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printFillRectangle()
	{
		out.print("\t/**\n" +
				"\t * Sets a rectangle inside the image with the specified value.\n" +
				"\t */\n" +
				"\tpublic static void fillRectangle("+imageName+" img, "+imageType.getSumType()+" value, int x0, int y0, int width, int height) {\n" +
				"\t\tint x1 = x0 + width;\n" +
				"\t\tint y1 = y0 + height;\n" +
				"\n" +
				"\t\tfor (int y = y0; y < y1; y++) {\n" +
				"\t\t\tfor (int x = x0; x < x1; x++) {\n" +
				"\t\t\t\tif( img.isInBounds(x,y ))\n" +
				"\t\t\t\t\timg.set(x, y, value);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printRandomize() {

		String sumType = imageType.getSumType();
		String typeCast = imageType.getTypeCastFromSum();

		out.print("\t/**\n" +
				"\t * Sets each value in the image to a value drawn from an uniform distribution that has a range of min <= X < max.\n" +
				"\t */\n" +
				"\tpublic static void randomize("+imageName+" img, Random rand , "+sumType+" min , "+sumType+" max) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+sumType+" range = max-min;\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n");
		if( imageType.isInteger() ) {
			out.print("\t\t\t\tdata[index++] = "+typeCast+"(rand.nextInt(range)+min);\n");
		} else {
			String randType = imageType.getRandType();
			out.print("\t\t\t\tdata[index++] = rand.next"+randType+"()*range+min;\n");
		}
		out.print("\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printAddUniform() {

		String sumType = imageType.getSumType();
		int min = imageType.getMin().intValue();
		int max = imageType.getMax().intValue();
		String typeCast = imageType.getTypeCastFromSum();


		out.print("\t/**\n" +
				"\t * Adds noise to the image drawn from an uniform distribution that has a range of min <= X < max.\n" +
				"\t */\n" +
				"\tpublic static void addUniform("+imageName+" img, Random rand , "+sumType+" min , "+sumType+" max) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+sumType+" range = max-min;\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < w; x++) {\n");
		if( imageType.isInteger() ) {
			out.print("\t\t\t\t"+sumType+" value = (data[index] "+bitWise+") + rand.nextInt(range)+min;\n");
			if( imageType.getPrimitiveType() != int.class ) {
				out.print("\t\t\t\tif( value < "+min+" ) value = "+min+";\n" +
						"\t\t\t\tif( value > "+max+" ) value = "+max+";\n" +
						"\n");
			}
		} else {
			String randType = imageType.getRandType();
			out.print("\t\t\t\t"+sumType+" value = data[index] + rand.next"+randType+"()*range+min;\n");
		}
		out.print("\t\t\t\tdata[index++] = "+typeCast+" value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}


	public static void main( String args[] ) throws FileNotFoundException {
		GeneratorImageTestingOps gen = new GeneratorImageTestingOps();
		gen.generate();
	}
}