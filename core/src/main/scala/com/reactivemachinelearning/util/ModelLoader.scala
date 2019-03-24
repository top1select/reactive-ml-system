package com.reactivemachinelearning.util

import org.apache.spark.sql.catalyst.ScalaReflection

import scala.reflect.io.Path
import scala.reflect.runtime.universe.TypeTag
import org.apache.spark.sql.types.{DataType, StructField, StructType}
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{DefaultFormats, JValue}

trait Savable {

  /**
  * This saves: *  - human-readable (JSON) model metadata to path/metadata/
  *  - Parquet formatted data to path/data/
  */
  def save(path: String): Unit
}

trait Loader[M] {
  /**
    * Load a model from the given path.
    *
    * The model should have been saved.
    *
    * @param path Path is the directory to which the model is saved.
    * @return Model instance
    */
  def load(path: String): M
}

private[reactivemachinelearning] object ModelLoader {

  /** Returns URI for path/data using the scala filesystem */
  def dataPath(path: String): String = new Path(path, "data").toURI.toString

  /** Returns URI for path/metadata using the scala filesystem */
  def metadataPath(path: String): String = new Path(path, "metadata").toURI.toString

  /**
    * Check the schema of loaded model data.
    *
    * This checks every field in the expected schema to make sure that a field with the same
    * name and DataType appears in the loaded schema.  Note that this does NOT check metadata
    * or containsNull.
    *
    * @param loadedSchema  Schema for model data loaded from file.
    * @tparam Data  Expected data type from which an expected schema can be derived.
    */
  def checkSchema[Data: TypeTag](loadedSchema: StructType): Unit = {
    // Check schema explicitly since erasure makes it hard to use match-case
    val expectedFields: Array[StructField] =
      ScalaReflection.schemaFor[Data].dataType.asInstanceOf[StructType].fields
    val loadedFields: Map[String, DataType] =
      loadedSchema.map(field => field.name -> field.dataType).toMap
    expectedFields.foreach{ field =>
      assert(loadedFields.contains(field.name), s"Unable to parse model data." +
      s" Expected field with name ${field.name} was missing in loaded schema:" +
        s" ${loadedFields.mkString(", ")}")
      // sameType
      assert(loadedFields(field.name) == field.dataType,
        s"Unable to parse model data.  Expected field $field but found field" +
          s" with different type: ${loadedFields(field.name)}")
    }
  }

  def loadMetadata(path: String): (String, String, JValue) = {
    implicit val formats = DefaultFormats
    val metadata = parse(path)
    val clazz = (metadata \ "class").extract[String]
    val version = (metadata \ "version").extract[String]
    (clazz, version, metadata)
  }
}
