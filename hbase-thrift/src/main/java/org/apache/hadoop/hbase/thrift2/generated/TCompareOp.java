/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.hadoop.hbase.thrift2.generated;


/**
 * Thrift wrapper around
 * org.apache.hadoop.hbase.filter.CompareFilter$CompareOp.
 */
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2020-02-01")
public enum TCompareOp implements org.apache.thrift.TEnum {
  LESS(0),
  LESS_OR_EQUAL(1),
  EQUAL(2),
  NOT_EQUAL(3),
  GREATER_OR_EQUAL(4),
  GREATER(5),
  NO_OP(6);

  private final int value;

  private TCompareOp(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static TCompareOp findByValue(int value) { 
    switch (value) {
      case 0:
        return LESS;
      case 1:
        return LESS_OR_EQUAL;
      case 2:
        return EQUAL;
      case 3:
        return NOT_EQUAL;
      case 4:
        return GREATER_OR_EQUAL;
      case 5:
        return GREATER;
      case 6:
        return NO_OP;
      default:
        return null;
    }
  }
}
