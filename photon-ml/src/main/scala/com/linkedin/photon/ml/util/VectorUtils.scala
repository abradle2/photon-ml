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


import breeze.linalg.{DenseVector, SparseVector, Vector}
import org.apache.spark.mllib.linalg.{DenseVector => SDV, SparseVector => SSV, Vector => SparkVector}

import scala.collection.mutable


/**
 * A utility object that contains some operations on [[Vector]].
 */
object VectorUtils {

  protected[ml] val SPARSE_VECTOR_ACTIVE_SIZE_TO_SIZE_RATIO: Double = 1.0 / 3

  /**
   * Convert an [[Array]] of ([[Int]], [[Double]]) pairs into a [[Vector]].
   * @param indexAndData An [[Array]] of ([[Int]], [[Double]]) pairs of indices and data to be converted to a [[Vector]]
   * @param length The length of the resulting vector
   * @param sparseVectorActiveSizeToSizeRatio The ratio used to determine whether a [[DenseVector]] or a
   *                                          [[SparseVector]] should be used to represent the underlying [[Vector]],
   *                                          for example, if the active size of the underlying vector is smaller than
   *                                          the length * sparseVectorActiveSizeToSizeRatio, then the [[SparseVector]]
   *                                          is chosen to represent the underlying [[Vector]], otherwise
   *                                          [[DenseVector]] is chosen.
   * @return The converted [[Vector]]
   */
  protected[ml] def convertIndexAndValuePairArrayToVector(
      indexAndData: Array[(Int, Double)],
      length: Int,
      sparseVectorActiveSizeToSizeRatio: Double = SPARSE_VECTOR_ACTIVE_SIZE_TO_SIZE_RATIO): Vector[Double] = {

    if (length * SPARSE_VECTOR_ACTIVE_SIZE_TO_SIZE_RATIO < indexAndData.length) {
      convertIndexAndValuePairArrayToDenseVector(indexAndData, length)
    } else {
      convertIndexAndValuePairArrayToSparseVector(indexAndData, length)
    }
  }

  /**
   * Convert an [[Array]] of ([[Int]], [[Double]]) pairs into a [[SparseVector]]
   * @param indexAndData An [[Array]] of ([[Int]], [[Double]]) pairs
   * @param length The length of the resulting sparse vector
   * @return The converted [[SparseVector]]
   */
  protected[ml] def convertIndexAndValuePairArrayToSparseVector(indexAndData: Array[(Int, Double)], length: Int)
  : SparseVector[Double] = {

    val sortedIndexAndData = indexAndData.sortBy(_._1)
    val index = new Array[Int](sortedIndexAndData.length)
    val data = new Array[Double](sortedIndexAndData.length)
    var i = 0
    while (i < sortedIndexAndData.length) {
      index(i) = sortedIndexAndData(i)._1
      data(i) = sortedIndexAndData(i)._2
      i += 1
    }
    new SparseVector[Double](index, data, length)
  }

  /**
   * Convert an [[Array]] of ([[Int]], [[Double]]) pairs into a [[DenseVector]]
   * @param indexAndData An [[Array]] of ([[Int]], [[Double]]) pairs
   * @param length The length of the resulting dense vector
   * @return The converted [[DenseVector]]
   */
  protected[ml] def convertIndexAndValuePairArrayToDenseVector(indexAndData: Array[(Int, Double)], length: Int)
  : DenseVector[Double] = {

    val dataArray = new Array[Double](length)
    var i = 0
    while (i < indexAndData.length) {
      val (index, data) = indexAndData(i)
      dataArray(index) = data
      i += 1
    }
    new DenseVector[Double](dataArray)
  }

  /**
   * The Kronecker product between two vectors: vector1 and vector2
   * Wiki reference on the Kronecker product: [[https://en.wikipedia.org/wiki/Kronecker_product]]
   * @param vector1 The left vector
   * @param vector2 The right vector
   * @param threshold Threshold of the cross value
   * @return The resulting Kronecker product between vector1 and vector2
   */
  protected[ml] def kroneckerProduct(vector1: Vector[Double], vector2: Vector[Double], threshold: Double)
  : Vector[Double] = {

    assert(vector1.isInstanceOf[SparseVector[Double]] || vector2.isInstanceOf[SparseVector[Double]],
      "Kronecker product between two dense vectors is currently not supported!")

    val length = vector1.length * vector2.length
    val activeSize = vector1.activeSize * vector2.activeSize
    val index = new mutable.ArrayBuffer[Int](activeSize)
    val data = new mutable.ArrayBuffer[Double](activeSize)
    for ( (idx1, data1) <- vector1.activeIterator) {
      for ( (idx2, data2) <- vector2.activeIterator ) {
        val crossedValue = data1 * data2
        if (math.abs(crossedValue) > threshold) {
          val idx = idx1 * vector2.length + idx2
          index += idx
          data += crossedValue
        }
      }
    }
    new SparseVector[Double](index.toArray, data.toArray, length)
  }

  /**
    * Converts a Breeze vector to an mllib vector
    *
    * @param breezeVector the Breeze vector
    * @return the mllib vector
    * @note lifted from spark private api
    */
  def breezeToMllib(breezeVector: Vector[Double]): SparkVector = {
    breezeVector match {
      case v: DenseVector[Double] =>
        if (v.offset == 0 && v.stride == 1 && v.length == v.data.length) {
          new SDV(v.data)
        } else {
          new SDV(v.toArray)  // Can't use underlying array directly, so make a new one
        }

      case v: SparseVector[Double] =>
        if (v.index.length == v.used) {
          new SSV(v.length, v.index, v.data)
        } else {
          new SSV(v.length, v.index.slice(0, v.used), v.data.slice(0, v.used))
        }

      case v: Vector[_] =>
        throw new IllegalArgumentException("Unsupported Breeze vector type: " + v.getClass.getName)
    }
  }

  /**
    * Converts a mllib vector to a Breeze vector
    *
    * @param mllibVector the mllib vector
    * @return the Breeze vector
    * @note lifted from spark private api
    */
  def mllibToBreeze(mllibVector: SparkVector): Vector[Double] =
    mllibVector match {
      case v: SSV =>
        new SparseVector[Double](v.indices, v.values, v.size)

      case v: SDV =>
        new DenseVector[Double](v.values)

      case v =>
        throw new IllegalArgumentException("Unsupported mllib vector type: " + v.getClass.getName)
    }
}
