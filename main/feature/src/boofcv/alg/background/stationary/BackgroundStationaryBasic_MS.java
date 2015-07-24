/*
 * Copyright (c) 2011-2015, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.background.stationary;

import boofcv.alg.InputSanityCheck;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.core.image.FactoryGImageMultiBand;
import boofcv.core.image.GConvertImage;
import boofcv.core.image.GImageMultiBand;
import boofcv.struct.image.*;

/**
 * Implementation of {@link BackgroundStationaryBasic} for {@link ImageSingleBand}.
 *
 * @author Peter Abeles
 */
public class BackgroundStationaryBasic_MS<T extends ImageSingleBand>
	extends BackgroundStationaryBasic<MultiSpectral<T>>
{
	// storage for background image
	protected MultiSpectral<ImageFloat32> background;

	// wrapper which provides abstraction across image types
	protected GImageMultiBand inputWrapper;
	protected GImageMultiBand bgWrapper;

	protected float inputPixels[];
	protected float bgPixels[];

	public BackgroundStationaryBasic_MS(float learnRate, float threshold,
										ImageType<MultiSpectral<T>> imageType) {
		super(learnRate, threshold, imageType);

		int numBands = imageType.getNumBands();

		background = new MultiSpectral<ImageFloat32>(ImageFloat32.class,1,1,numBands);
		bgWrapper = FactoryGImageMultiBand.create(background.getImageType());
		bgWrapper.wrap(background);

		inputWrapper = FactoryGImageMultiBand.create(imageType);

		inputPixels = new float[numBands];
		bgPixels = new float[numBands];
	}

	/**
	 * Returns the background image.
	 *
	 * @return background image.
	 */
	public MultiSpectral<ImageFloat32> getBackground() {
		return background;
	}

	@Override
	public void reset() {
		background.reshape(1,1);
	}

	@Override
	public void updateBackground( MultiSpectral<T> frame) {
		if( background.width == 1 ) {
			background.reshape(frame.width, frame.height);
			GConvertImage.convert(frame, background);
			return;
		} else {
			InputSanityCheck.checkSameShape(background,frame);
		}

		inputWrapper.wrap(frame);

		int numBands = background.getNumBands();
		float minusLearn = 1.0f - learnRate;

		int indexBG = 0;
		for (int y = 0; y < frame.height; y++) {
			int indexInput = frame.startIndex + y*frame.stride;
			int end = indexInput + frame.width;
			while( indexInput < end ) {
				inputWrapper.getF(indexInput, inputPixels);
				bgWrapper.getF(indexBG, bgPixels);

				for (int band = 0; band < numBands; band++) {
					bgPixels[band] = minusLearn*bgPixels[band] + learnRate*inputPixels[band];
				}
				bgWrapper.setF(indexBG,bgPixels);

				indexInput++;
				indexBG++;
			}
		}
	}

	@Override
	public void segment(MultiSpectral<T> frame, ImageUInt8 segmented) {
		if( background.width == 1 ) {
			ImageMiscOps.fill(segmented,unknownValue);
			return;
		}
		InputSanityCheck.checkSameShape(background,frame,segmented);
		inputWrapper.wrap(frame);

		int numBands = background.getNumBands();
		float thresholdSq = numBands*threshold*threshold;

		int indexBG = 0;
		for (int y = 0; y < frame.height; y++) {
			int indexInput = frame.startIndex + y*frame.stride;
			int indexSegmented = segmented.startIndex + y*segmented.stride;

			int end = indexInput + frame.width;
			while( indexInput < end ) {
				inputWrapper.getF(indexInput, inputPixels);
				bgWrapper.getF(indexBG, bgPixels);

				double sumErrorSq = 0;
				for (int band = 0; band < numBands; band++) {
					float diff = bgPixels[band] - inputPixels[band];
					sumErrorSq += diff*diff;
				}


				if (sumErrorSq <= thresholdSq) {
					segmented.data[indexSegmented] = 0;
				} else {
					segmented.data[indexSegmented] = 1;
				}

				indexInput++;
				indexSegmented++;
				indexBG++;
			}
		}
	}


}