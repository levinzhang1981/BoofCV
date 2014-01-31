/*
 * Copyright (c) 2011-2014, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.segmentation.fh04.impl;

import boofcv.alg.segmentation.fh04.FhEdgeWeights;
import boofcv.struct.image.ImageFloat32;
import org.ddogleg.struct.FastQueue;

import static boofcv.alg.segmentation.fh04.SegmentFelzenHutten04.Edge;

/**
 * <p>Computes edge weight as the absolute value of the different in pixel value for single band images.
 * A 4-connect neighborhood is considered.</p>
 *
 * <p>
 * WARNING: Do not modify.  Automatically generated by {@link GenerateFhEdgeWeights_SB}.
 * </p>
 *
 * @author Peter Abeles
 */
public class FhEdgeWeights4_F32 implements FhEdgeWeights<ImageFloat32> {

	@Override
	public void process(ImageFloat32 input,
						FastQueue<Edge> edges) {

		int w = input.width-1;
		int h = input.height-1;

		// First consider the inner pixels
		for( int y = 0; y < h; y++ ) {
			int indexSrc = input.startIndex + y*input.stride + 0;
			int indexDst =                  + y*input.width  + 0;

			for( int x = 0; x < w; x++ , indexSrc++ , indexDst++ ) {
				float color0 = input.data[indexSrc];              // (x,y)
				float color1 = input.data[indexSrc+1];            // (x+1,y)
				float color2 = input.data[indexSrc+input.stride]; // (x,y+1)

				Edge e1 = edges.grow();
				Edge e2 = edges.grow();

				e1.sortValue = Math.abs(color1-color0);
				e1.indexA = indexDst;
				e1.indexB = indexDst+1;

				e2.sortValue = Math.abs(color2-color0);
				e2.indexA = indexDst;
				e2.indexB = indexDst+input.width;
			}
		}
		// Handle border pixels
		for( int y = 0; y < h; y++ ) {
			checkAround(w,y,input,edges);
		}

		for( int x = 0; x < w; x++ ) {
			checkAround(x,h,input,edges);
		}
	}
	private void checkAround( int x , int y ,
							  ImageFloat32 input ,
							  FastQueue<Edge> edges )
	{
		int indexSrc = input.startIndex + y*input.stride + x;
		int indexA =                      y*input.width  + x;

		float color0 = input.data[indexSrc];

		check(x+1,y  ,color0,indexA,input,edges);
		check(x  ,y+1,color0,indexA,input,edges);
	}

	private void check( int x , int y , float color0 , int indexA,
						ImageFloat32 input ,
						FastQueue<Edge> edges ) {
		if( !input.isInBounds(x,y) )
			return;

		int indexSrc = input.startIndex + y*input.stride + x;
		int indexB   =                  + y*input.width  + x;

		float colorN = input.data[indexSrc];

		Edge e1 = edges.grow();

		e1.sortValue = (float)Math.abs(color0-colorN);
		e1.indexA = indexA;
		e1.indexB = indexB;
	}

}
