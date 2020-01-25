/**
 * Autogenerated by Thrift Compiler (0.12.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.hadoop.hbase.thrift2.generated;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.12.0)", date = "2020-02-01")
public class TTimeRange implements org.apache.thrift.TBase<TTimeRange, TTimeRange._Fields>, java.io.Serializable, Cloneable, Comparable<TTimeRange> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TTimeRange");

  private static final org.apache.thrift.protocol.TField MIN_STAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("minStamp", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField MAX_STAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("maxStamp", org.apache.thrift.protocol.TType.I64, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TTimeRangeStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TTimeRangeTupleSchemeFactory();

  public long minStamp; // required
  public long maxStamp; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    MIN_STAMP((short)1, "minStamp"),
    MAX_STAMP((short)2, "maxStamp");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // MIN_STAMP
          return MIN_STAMP;
        case 2: // MAX_STAMP
          return MAX_STAMP;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __MINSTAMP_ISSET_ID = 0;
  private static final int __MAXSTAMP_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MIN_STAMP, new org.apache.thrift.meta_data.FieldMetaData("minStamp", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.MAX_STAMP, new org.apache.thrift.meta_data.FieldMetaData("maxStamp", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TTimeRange.class, metaDataMap);
  }

  public TTimeRange() {
  }

  public TTimeRange(
    long minStamp,
    long maxStamp)
  {
    this();
    this.minStamp = minStamp;
    setMinStampIsSet(true);
    this.maxStamp = maxStamp;
    setMaxStampIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TTimeRange(TTimeRange other) {
    __isset_bitfield = other.__isset_bitfield;
    this.minStamp = other.minStamp;
    this.maxStamp = other.maxStamp;
  }

  public TTimeRange deepCopy() {
    return new TTimeRange(this);
  }

  @Override
  public void clear() {
    setMinStampIsSet(false);
    this.minStamp = 0;
    setMaxStampIsSet(false);
    this.maxStamp = 0;
  }

  public long getMinStamp() {
    return this.minStamp;
  }

  public TTimeRange setMinStamp(long minStamp) {
    this.minStamp = minStamp;
    setMinStampIsSet(true);
    return this;
  }

  public void unsetMinStamp() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MINSTAMP_ISSET_ID);
  }

  /** Returns true if field minStamp is set (has been assigned a value) and false otherwise */
  public boolean isSetMinStamp() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MINSTAMP_ISSET_ID);
  }

  public void setMinStampIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MINSTAMP_ISSET_ID, value);
  }

  public long getMaxStamp() {
    return this.maxStamp;
  }

  public TTimeRange setMaxStamp(long maxStamp) {
    this.maxStamp = maxStamp;
    setMaxStampIsSet(true);
    return this;
  }

  public void unsetMaxStamp() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MAXSTAMP_ISSET_ID);
  }

  /** Returns true if field maxStamp is set (has been assigned a value) and false otherwise */
  public boolean isSetMaxStamp() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MAXSTAMP_ISSET_ID);
  }

  public void setMaxStampIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MAXSTAMP_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case MIN_STAMP:
      if (value == null) {
        unsetMinStamp();
      } else {
        setMinStamp((java.lang.Long)value);
      }
      break;

    case MAX_STAMP:
      if (value == null) {
        unsetMaxStamp();
      } else {
        setMaxStamp((java.lang.Long)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case MIN_STAMP:
      return getMinStamp();

    case MAX_STAMP:
      return getMaxStamp();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case MIN_STAMP:
      return isSetMinStamp();
    case MAX_STAMP:
      return isSetMaxStamp();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof TTimeRange)
      return this.equals((TTimeRange)that);
    return false;
  }

  public boolean equals(TTimeRange that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_minStamp = true;
    boolean that_present_minStamp = true;
    if (this_present_minStamp || that_present_minStamp) {
      if (!(this_present_minStamp && that_present_minStamp))
        return false;
      if (this.minStamp != that.minStamp)
        return false;
    }

    boolean this_present_maxStamp = true;
    boolean that_present_maxStamp = true;
    if (this_present_maxStamp || that_present_maxStamp) {
      if (!(this_present_maxStamp && that_present_maxStamp))
        return false;
      if (this.maxStamp != that.maxStamp)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(minStamp);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(maxStamp);

    return hashCode;
  }

  @Override
  public int compareTo(TTimeRange other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetMinStamp()).compareTo(other.isSetMinStamp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMinStamp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.minStamp, other.minStamp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMaxStamp()).compareTo(other.isSetMaxStamp());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMaxStamp()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.maxStamp, other.maxStamp);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("TTimeRange(");
    boolean first = true;

    sb.append("minStamp:");
    sb.append(this.minStamp);
    first = false;
    if (!first) sb.append(", ");
    sb.append("maxStamp:");
    sb.append(this.maxStamp);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'minStamp' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'maxStamp' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TTimeRangeStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TTimeRangeStandardScheme getScheme() {
      return new TTimeRangeStandardScheme();
    }
  }

  private static class TTimeRangeStandardScheme extends org.apache.thrift.scheme.StandardScheme<TTimeRange> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TTimeRange struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MIN_STAMP
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.minStamp = iprot.readI64();
              struct.setMinStampIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // MAX_STAMP
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.maxStamp = iprot.readI64();
              struct.setMaxStampIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetMinStamp()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'minStamp' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetMaxStamp()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'maxStamp' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TTimeRange struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(MIN_STAMP_FIELD_DESC);
      oprot.writeI64(struct.minStamp);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(MAX_STAMP_FIELD_DESC);
      oprot.writeI64(struct.maxStamp);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TTimeRangeTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TTimeRangeTupleScheme getScheme() {
      return new TTimeRangeTupleScheme();
    }
  }

  private static class TTimeRangeTupleScheme extends org.apache.thrift.scheme.TupleScheme<TTimeRange> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TTimeRange struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      oprot.writeI64(struct.minStamp);
      oprot.writeI64(struct.maxStamp);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TTimeRange struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      struct.minStamp = iprot.readI64();
      struct.setMinStampIsSet(true);
      struct.maxStamp = iprot.readI64();
      struct.setMaxStampIsSet(true);
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

