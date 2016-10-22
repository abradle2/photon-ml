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
package com.linkedin.photon.ml.evaluation

import org.testng.Assert._
import org.testng.annotations.{DataProvider, Test}

import com.linkedin.photon.ml.constants.MathConst

class PrecisionAtKLocalEvaluatorTest {

  import TestUtilFunctions.getScoreLabelAndWeights

  @DataProvider
  def getEvaluateTestCases: Array[Array[Any]] = {

    // Trivial case but with different Ks
    val trivialScores = Array(1.0, 0.5, 0.0)
    val labelsWithPosOn1 = Array(1.0, 0.0, 0.0)
    val labelsWithPosOn2 = Array(0.0, 1.0, 0.0)
    val labelsWithPosOn3 = Array(0.0, 0.0, 1.0)
    val labelsWithNoPos = Array(0.0, 0.0, 0.0)
    val ks = (1 to 4).toArray
    val trivialCase1 = ks.map{ k =>
      val expectedResult = 1.0 / k
      Array(k, getScoreLabelAndWeights(trivialScores, labelsWithPosOn1), expectedResult)
    }
    val trivialCase2 = ks.map{ k =>
      val expectedResult = if (k == 1) 0.0 else 1.0 / k
      Array(k, getScoreLabelAndWeights(trivialScores, labelsWithPosOn2), expectedResult)
    }
    val trivialCase3 = ks.map{ k =>
      val expectedResult = if (k <= 2) 0.0 else 1.0 / k
      Array(k, getScoreLabelAndWeights(trivialScores, labelsWithPosOn3), expectedResult)
    }
    val trivialCase4 = ks.map{ k =>
      val expectedResult = 0.0
      Array(k, getScoreLabelAndWeights(trivialScores, labelsWithNoPos), expectedResult)
    }

    // Two case adopted from Metronome
    val labelsM1 = Array[Double](0, 0, 1, 0)
    val scoresM1 = Array(1, 0.75, 0.5, 0.25)
    val metronomeCase1 = Array(
      Array (1, getScoreLabelAndWeights(scoresM1, labelsM1), 0.0),
      Array (2, getScoreLabelAndWeights(scoresM1, labelsM1), 0.0),
      Array (3, getScoreLabelAndWeights(scoresM1, labelsM1), 1.0 / 3),
      Array (4, getScoreLabelAndWeights(scoresM1, labelsM1), 1.0 / 4),
      Array (10, getScoreLabelAndWeights(scoresM1, labelsM1), 1.0 / 10)
    )

    val labelsM2 = Array[Double](1, 0, 0, 1, 1, 0)
    val scoresM2 = Array(1, 0.75, 0.5, 0.25, 0.2, 0.1)
    val metronomeCase2 = Array(
      Array (1, getScoreLabelAndWeights(scoresM2, labelsM2), 1.0),
      Array (2, getScoreLabelAndWeights(scoresM2, labelsM2), 1.0 / 2),
      Array (3, getScoreLabelAndWeights(scoresM2, labelsM2), 1.0 / 3),
      Array (4, getScoreLabelAndWeights(scoresM2, labelsM2), 2.0 / 4),
      Array (5, getScoreLabelAndWeights(scoresM2, labelsM2), 3.0 / 5),
      Array (100, getScoreLabelAndWeights(scoresM2, labelsM2), 3.0 / 100)
    )

    trivialCase1 ++ trivialCase2 ++ trivialCase3 ++ trivialCase4 ++ metronomeCase1 ++ metronomeCase2
  }

  @Test(dataProvider = "getEvaluateTestCases")
  def testEvaluate(
    k: Int,
    scoreLabelAndWeights: Array[(Double, Double, Double)],
    expectedResult: Double): Unit = {

    val evaluator = new PrecisionAtKLocalEvaluator(k)
    val actualResult = evaluator.evaluate(scoreLabelAndWeights)
    assertEquals(actualResult, expectedResult, MathConst.MEDIUM_PRECISION_TOLERANCE_THRESHOLD)
  }
}
