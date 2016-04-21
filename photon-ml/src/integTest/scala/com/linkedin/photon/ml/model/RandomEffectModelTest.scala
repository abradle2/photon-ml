/*
 * Copyright 2016 LinkedIn Corp. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linkedin.photon.ml.model

import org.testng.annotations.Test
import org.testng.Assert.assertTrue

import com.linkedin.photon.ml.test.SparkTestUtils


/**
 * @author xazhang
 */
class RandomEffectModelTest extends SparkTestUtils {

  @Test
  def testEquals() = sparkTest("testEqualsForRandomEffectModel") {

    // Coefficients parameter
    val coefficientDimension = 1
    val coefficients = Coefficients.initializeZeroCoefficients(coefficientDimension)

    // Meta data
    val featureShardId = "featureShardId"
    val randomEffectId = "randomEffectId"

    // Random effect model
    val numCoefficients = 5
    val coefficientsRDD = sc.parallelize(Seq.tabulate(numCoefficients)(i => (i.toString, coefficients)))
    val randomEffectModel = new RandomEffectModel(coefficientsRDD, randomEffectId, featureShardId)

    // Should equal to itself
    assertTrue(randomEffectModel.equals(randomEffectModel))

    // Should equal to the random effect model with same featureShardId, randomEffectId and coefficientsRDD
    val randomEffectModelCopy = new RandomEffectModel(coefficientsRDD, randomEffectId, featureShardId)
    assertTrue(randomEffectModel.equals(randomEffectModelCopy))

    // Should not equal to the random effect model with different featureShardId
    val featureShardId1 = "featureShardId1"
    val randomEffectModelWithDiffFeatureShardId =
      new RandomEffectModel(coefficientsRDD, randomEffectId, featureShardId1)
    assertTrue(!randomEffectModel.equals(randomEffectModelWithDiffFeatureShardId))

    // Should not equal to the random effect model with different randomEffectId
    val randomEffectId1 = "randomEffectId1"
    val randomEffectModelWithDiffRandomEffectShardId =
      new RandomEffectModel(coefficientsRDD, randomEffectId1, featureShardId)
    assertTrue(!randomEffectModel.equals(randomEffectModelWithDiffRandomEffectShardId))

    // Should not equal to the random effect model with different coefficientsRDD
    val numCoefficients1 = numCoefficients + 1
    val coefficientsRDD1 = sc.parallelize(Seq.tabulate(numCoefficients1)(i => (i.toString, coefficients)))
    val randomEffectModelWithDiffCoefficientsRDD =
      new RandomEffectModel(coefficientsRDD1, randomEffectId, featureShardId)
    assertTrue(!randomEffectModel.equals(randomEffectModelWithDiffCoefficientsRDD))
  }
}