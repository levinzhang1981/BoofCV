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

package boofcv.factory.segmentation;

import boofcv.alg.interpolate.InterpolatePixelMB;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.alg.segmentation.ComputeRegionMeanColor;
import boofcv.alg.segmentation.fh04.FhEdgeWeights;
import boofcv.alg.segmentation.fh04.SegmentFelzenHutten04;
import boofcv.alg.segmentation.fh04.impl.*;
import boofcv.alg.segmentation.ms.*;
import boofcv.alg.weights.WeightDistanceUniform_F32;
import boofcv.alg.weights.WeightDistance_F32;
import boofcv.alg.weights.WeightPixelUniform_F32;
import boofcv.alg.weights.WeightPixel_F32;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

/**
 * Factory for segmentation algorithms.
 *
 * @author Peter Abeles
 */
public class FactorySegmentationAlg {

	/**
	 * Creates an instance of {@link boofcv.alg.segmentation.ComputeRegionMeanColor} for the specified image type.
	 *
	 * @param imageType image type
	 * @return ComputeRegionMeanColor
	 */
	public static <T extends ImageBase>
	ComputeRegionMeanColor<T> regionMeanColor(ImageType<T> imageType) {
		if( imageType.getFamily() == ImageType.Family.SINGLE_BAND ) {
			switch( imageType.getDataType() ) {
				case U8:
					return (ComputeRegionMeanColor)new ComputeRegionMeanColor.U8();
				case F32:
					return (ComputeRegionMeanColor)new ComputeRegionMeanColor.F32();
			}
		} else if( imageType.getFamily() == ImageType.Family.MULTI_SPECTRAL ) {
			int N = imageType.getNumBands();
			switch( imageType.getDataType() ) {
				case U8:
					return (ComputeRegionMeanColor)new ComputeRegionMeanColor.MS_U8(N);
				case F32:
					return (ComputeRegionMeanColor)new ComputeRegionMeanColor.MS_F32(N);
			}
		}

		throw new IllegalArgumentException("Unknown imageType");
	}

	/**
	 * Creates an instance of {@link boofcv.alg.segmentation.ms.SegmentMeanShift}.  Uniform distributions are used for spacial and color
	 * weights.
	 *
	 * @param spacialRadius Radius of mean-shift region in pixels. Try 6
	 * @param colorRadius Radius of mean-shift region for color in Euclidean distance. Try 15
	 * @param minimumRegionSize Minimum allowed size of a region in pixels. Try 30
	 * @param fast Improve runtime by approximating running mean-shift on each pixel. Try true.
	 * @param imageType Type of input image
	 * @return SegmentMeanShift
	 */
	public static<T extends ImageBase>
	SegmentMeanShift<T> meanShift( int spacialRadius , float colorRadius , int minimumRegionSize ,
								   boolean fast ,  ImageType<T> imageType )
	{
		WeightPixel_F32 weightSpacial = new WeightPixelUniform_F32(spacialRadius,spacialRadius);
		WeightDistance_F32 weightColor = new WeightDistanceUniform_F32(colorRadius*colorRadius);

		int maxIterations = 20;
		float convergenceTol = 0.1f;

		SegmentMeanShiftSearch<T> search;

		if( imageType.getFamily() == ImageType.Family.SINGLE_BAND ) {
			InterpolatePixelS interp = FactoryInterpolation.bilinearPixelS(imageType.getImageClass());
			search = new SegmentMeanShiftSearchGray(maxIterations,convergenceTol,interp,weightSpacial,weightColor,fast);
		} else {
			InterpolatePixelMB interp = FactoryInterpolation.createPixelMB(0,255,
					TypeInterpolate.BILINEAR,(ImageType)imageType);
			search = new SegmentMeanShiftSearchColor(maxIterations,convergenceTol,interp,
					weightSpacial,weightColor,fast,imageType);
		}

		ComputeRegionMeanColor<T> regionColor = regionMeanColor(imageType);
		MergeRegionMeanShift merge = new MergeRegionMeanShift(3,colorRadius/2);

		PruneSmallRegions<T> prune = minimumRegionSize >= 2 ?
				new PruneSmallRegions<T>(minimumRegionSize,regionColor) : null;

		return new SegmentMeanShift<T>(search,merge,prune,4);
	}

	public static <T extends ImageBase>
	FhEdgeWeights<T> weightsFelzenszwalb04( ConnectRule rule , ImageType<T> imageType) {
		if( imageType.getFamily() == ImageType.Family.SINGLE_BAND ) {
			if( rule == ConnectRule.FOUR ) {
				switch( imageType.getDataType() ) {
					case U8:
						return (FhEdgeWeights)new FhEdgeWeights4_U8();
					case F32:
						return (FhEdgeWeights)new FhEdgeWeights4_F32();
				}
			} else if( rule == ConnectRule.EIGHT ) {
				switch( imageType.getDataType() ) {
					case U8:
						return (FhEdgeWeights)new FhEdgeWeights8_U8();
					case F32:
						return (FhEdgeWeights)new FhEdgeWeights8_F32();
				}
			}
		} else if( imageType.getFamily() == ImageType.Family.MULTI_SPECTRAL ) {
			int N = imageType.getNumBands();
			if( rule == ConnectRule.FOUR ) {
				switch( imageType.getDataType() ) {
					case U8:
						return (FhEdgeWeights)new FhEdgeWeights4_MsU8(N);
					case F32:
						return (FhEdgeWeights)new FhEdgeWeights4_MsF32(N);
				}
			} else if( rule == ConnectRule.EIGHT ) {
				switch( imageType.getDataType() ) {
					case U8:
						return (FhEdgeWeights)new FhEdgeWeights8_MsU8(N);
					case F32:
						return (FhEdgeWeights)new FhEdgeWeights8_MsF32(N);
				}
			}
		}

		throw new IllegalArgumentException("Unknown imageType or connect rule");
	}

	public static<T extends ImageBase>
	SegmentFelzenHutten04<T> felzenszwalb04( int K , int minimumRegionSize , ConnectRule rule , ImageType<T> imageType )
	{

		FhEdgeWeights<T> edgeWeights = weightsFelzenszwalb04(rule,imageType);

		return new SegmentFelzenHutten04<T>(K,minimumRegionSize,edgeWeights);
	}
}
