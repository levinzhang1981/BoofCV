/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.feature.describe.impl;

import boofcv.abst.filter.blur.BlurFilter;
import boofcv.alg.feature.describe.DescribePointBrief;
import boofcv.alg.feature.describe.brief.BriefDefinition_I32;
import boofcv.struct.feature.TupleDesc_B;
import boofcv.struct.image.ImageUInt8;

import java.util.Arrays;

/**
 * <p>
 * Implementation of {@link DescribePointBrief} for a specific image type.
 * </p>
 *
 * <p>
 * WARNING: Do not modify.  Automatically generated by {@link FactoryImplDescribePointBrief}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplDescribePointBrief_U8 extends DescribePointBrief<ImageUInt8> {

	public ImplDescribePointBrief_U8(BriefDefinition_I32 definition, BlurFilter<ImageUInt8> filterBlur) {
		super(definition, filterBlur);
	}

	@Override
	public void process( double X , double Y , TupleDesc_B feature )
	{
		int c_x = (int)X;
		int c_y = (int)Y;

		Arrays.fill(feature.data, 0);

		int index = blur.startIndex + blur.stride*c_y + c_x;

		for( int i = 0; i < definition.compare.length; i++ ) {
			int valA = blur.data[index + offsetsA[i]]& 0xFF;
			int valB = blur.data[index + offsetsB[i]]& 0xFF;

			if( valA < valB ) {
				feature.data[ i/32 ] |= 1 << (i % 32);
			}
		}
	}

}
