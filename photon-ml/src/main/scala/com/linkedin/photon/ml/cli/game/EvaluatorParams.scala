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
package com.linkedin.photon.ml.cli.game

import com.linkedin.photon.ml.evaluation.{EvaluatorType, ShardedEvaluatorType}

/**
 * Evaluator params common to GAME training and scoring.
 */
trait EvaluatorParams {
  /**
   * A list of evaluators separated by comma. E.g, AUC,Precision@1:documentId,Precision@3:documentId,Logistic_Loss
   */
  var evaluatorTypes: Seq[EvaluatorType] = Seq()

  /**
   * Get all id types used to compute precision@K
   */
  def getShardedEvaluatorIdTypes: Set[String] = {
    evaluatorTypes.flatMap {
      case shardedEvaluatorType: ShardedEvaluatorType => Some(shardedEvaluatorType.idType)
      case _ => None
    }
      .toSet
  }
}
