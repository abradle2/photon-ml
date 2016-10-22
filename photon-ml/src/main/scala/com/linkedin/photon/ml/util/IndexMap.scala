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
package com.linkedin.photon.ml.util


import scala.collection.immutable.Map

/**
  * This trait defines the methods supported by an index map
  */
trait IndexMap extends Map[String, Int] with Serializable {

  /**
    * Lazily compute and cache the feature dimension
    */
  lazy val featureDimension: Int = values.max + 1

  /**
    * Given an index, return the corresponding feature name
    *
    * @param idx the feature index
    * @return the feature name, null if not found
    */
  def getFeatureName(idx: Int): Option[String]

  /**
    * Given a feature string, return the index
    *
    * @param name the feature name
    * @return the feature index, IndexMap.NULL_KEY if not found
    */
  def getIndex(name: String): Int
}

object IndexMap {
  // The key to indicate a feature does not exist in the map
  val NULL_KEY:Int = -1

  // "global" namespace for situations where either there aren't multiple namespaces, or we want to set apart a global
  // namespace from subspaces
  val GLOBAL_NS: String = "global"
}
