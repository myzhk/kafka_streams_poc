/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package sean.kafka_streams_poc.avro.domain;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

import java.util.List;
import java.util.stream.Collectors;

@org.apache.avro.specific.AvroGenerated
public class ApprovalDetails extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -2566072390044963974L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ApprovalDetails\",\"namespace\":\"sean.kafka_streams_poc.avro.domain\",\"fields\":[{\"name\":\"token\",\"type\":{\"type\":\"record\",\"name\":\"Token\",\"fields\":[{\"name\":\"tokenId\",\"type\":\"string\"},{\"name\":\"type\",\"type\":{\"type\":\"enum\",\"name\":\"TokenType\",\"symbols\":[\"EventToken\",\"AllocToken\"]}},{\"name\":\"entity\",\"type\":{\"type\":\"enum\",\"name\":\"Entity\",\"symbols\":[\"Bloomberg\",\"TradeWeb\",\"Traiana\"]}}]}},{\"name\":\"ApprovalDetails\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"ApprovalDetail\",\"fields\":[{\"name\":\"allocId\",\"type\":\"string\"},{\"name\":\"entity\",\"type\":\"Entity\"},{\"name\":\"taserApprovalId\",\"type\":\"string\"},{\"name\":\"economics\",\"type\":\"string\"},{\"name\":\"marked\",\"type\":\"boolean\"}]}}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<ApprovalDetails> ENCODER =
      new BinaryMessageEncoder<ApprovalDetails>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<ApprovalDetails> DECODER =
      new BinaryMessageDecoder<ApprovalDetails>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<ApprovalDetails> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<ApprovalDetails> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<ApprovalDetails> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<ApprovalDetails>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this ApprovalDetails to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a ApprovalDetails from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a ApprovalDetails instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static ApprovalDetails fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private sean.kafka_streams_poc.avro.domain.Token token;
   private java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> ApprovalDetails;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public ApprovalDetails() {}

  /**
   * All-args constructor.
   * @param token The new value for token
   * @param ApprovalDetails The new value for ApprovalDetails
   */
  public ApprovalDetails(sean.kafka_streams_poc.avro.domain.Token token, java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> ApprovalDetails) {
    this.token = token;
    this.ApprovalDetails = ApprovalDetails;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return token;
    case 1: return ApprovalDetails;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: token = (sean.kafka_streams_poc.avro.domain.Token)value$; break;
    case 1: ApprovalDetails = (java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'token' field.
   * @return The value of the 'token' field.
   */
  public sean.kafka_streams_poc.avro.domain.Token getToken() {
    return token;
  }


  /**
   * Sets the value of the 'token' field.
   * @param value the value to set.
   */
  public void setToken(sean.kafka_streams_poc.avro.domain.Token value) {
    this.token = value;
  }

  /**
   * Gets the value of the 'ApprovalDetails' field.
   * @return The value of the 'ApprovalDetails' field.
   */
  public java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> getApprovalDetails() {
    return ApprovalDetails;
  }


  /**
   * Sets the value of the 'ApprovalDetails' field.
   * @param value the value to set.
   */
  public void setApprovalDetails(java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> value) {
    this.ApprovalDetails = value;
  }

  /**
   * Creates a new ApprovalDetails RecordBuilder.
   * @return A new ApprovalDetails RecordBuilder
   */
  public static sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder newBuilder() {
    return new sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder();
  }

  /**
   * Creates a new ApprovalDetails RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new ApprovalDetails RecordBuilder
   */
  public static sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder newBuilder(sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder other) {
    if (other == null) {
      return new sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder();
    } else {
      return new sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder(other);
    }
  }

  /**
   * Creates a new ApprovalDetails RecordBuilder by copying an existing ApprovalDetails instance.
   * @param other The existing instance to copy.
   * @return A new ApprovalDetails RecordBuilder
   */
  public static sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder newBuilder(sean.kafka_streams_poc.avro.domain.ApprovalDetails other) {
    if (other == null) {
      return new sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder();
    } else {
      return new sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder(other);
    }
  }

  public static ApprovalDetails fromApprovalDetails(sean.kafka_streams_poc.domain.ApprovalDetails approvalDetails) {
    sean.kafka_streams_poc.avro.domain.ApprovalDetails ret = new ApprovalDetails();
    ret.setToken(Token.fromToken(approvalDetails.token));
    List<ApprovalDetail> detailList = approvalDetails.approvalDetails.stream()
            .map(ApprovalDetail::fromApprovalDetail).collect(Collectors.toList());
    ret.setApprovalDetails(detailList);

    return ret;
  }

  /**
   * RecordBuilder for ApprovalDetails instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ApprovalDetails>
    implements org.apache.avro.data.RecordBuilder<ApprovalDetails> {

    private sean.kafka_streams_poc.avro.domain.Token token;
    private sean.kafka_streams_poc.avro.domain.Token.Builder tokenBuilder;
    private java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> ApprovalDetails;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.token)) {
        this.token = data().deepCopy(fields()[0].schema(), other.token);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (other.hasTokenBuilder()) {
        this.tokenBuilder = sean.kafka_streams_poc.avro.domain.Token.newBuilder(other.getTokenBuilder());
      }
      if (isValidValue(fields()[1], other.ApprovalDetails)) {
        this.ApprovalDetails = data().deepCopy(fields()[1].schema(), other.ApprovalDetails);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
    }

    /**
     * Creates a Builder by copying an existing ApprovalDetails instance
     * @param other The existing instance to copy.
     */
    private Builder(sean.kafka_streams_poc.avro.domain.ApprovalDetails other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.token)) {
        this.token = data().deepCopy(fields()[0].schema(), other.token);
        fieldSetFlags()[0] = true;
      }
      this.tokenBuilder = null;
      if (isValidValue(fields()[1], other.ApprovalDetails)) {
        this.ApprovalDetails = data().deepCopy(fields()[1].schema(), other.ApprovalDetails);
        fieldSetFlags()[1] = true;
      }
    }

    /**
      * Gets the value of the 'token' field.
      * @return The value.
      */
    public sean.kafka_streams_poc.avro.domain.Token getToken() {
      return token;
    }


    /**
      * Sets the value of the 'token' field.
      * @param value The value of 'token'.
      * @return This builder.
      */
    public sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder setToken(sean.kafka_streams_poc.avro.domain.Token value) {
      validate(fields()[0], value);
      this.tokenBuilder = null;
      this.token = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'token' field has been set.
      * @return True if the 'token' field has been set, false otherwise.
      */
    public boolean hasToken() {
      return fieldSetFlags()[0];
    }

    /**
     * Gets the Builder instance for the 'token' field and creates one if it doesn't exist yet.
     * @return This builder.
     */
    public sean.kafka_streams_poc.avro.domain.Token.Builder getTokenBuilder() {
      if (tokenBuilder == null) {
        if (hasToken()) {
          setTokenBuilder(sean.kafka_streams_poc.avro.domain.Token.newBuilder(token));
        } else {
          setTokenBuilder(sean.kafka_streams_poc.avro.domain.Token.newBuilder());
        }
      }
      return tokenBuilder;
    }

    /**
     * Sets the Builder instance for the 'token' field
     * @param value The builder instance that must be set.
     * @return This builder.
     */
    public sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder setTokenBuilder(sean.kafka_streams_poc.avro.domain.Token.Builder value) {
      clearToken();
      tokenBuilder = value;
      return this;
    }

    /**
     * Checks whether the 'token' field has an active Builder instance
     * @return True if the 'token' field has an active Builder instance
     */
    public boolean hasTokenBuilder() {
      return tokenBuilder != null;
    }

    /**
      * Clears the value of the 'token' field.
      * @return This builder.
      */
    public sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder clearToken() {
      token = null;
      tokenBuilder = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'ApprovalDetails' field.
      * @return The value.
      */
    public java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> getApprovalDetails() {
      return ApprovalDetails;
    }


    /**
      * Sets the value of the 'ApprovalDetails' field.
      * @param value The value of 'ApprovalDetails'.
      * @return This builder.
      */
    public sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder setApprovalDetails(java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> value) {
      validate(fields()[1], value);
      this.ApprovalDetails = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'ApprovalDetails' field has been set.
      * @return True if the 'ApprovalDetails' field has been set, false otherwise.
      */
    public boolean hasApprovalDetails() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'ApprovalDetails' field.
      * @return This builder.
      */
    public sean.kafka_streams_poc.avro.domain.ApprovalDetails.Builder clearApprovalDetails() {
      ApprovalDetails = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ApprovalDetails build() {
      try {
        ApprovalDetails record = new ApprovalDetails();
        if (tokenBuilder != null) {
          try {
            record.token = this.tokenBuilder.build();
          } catch (org.apache.avro.AvroMissingFieldException e) {
            e.addParentField(record.getSchema().getField("token"));
            throw e;
          }
        } else {
          record.token = fieldSetFlags()[0] ? this.token : (sean.kafka_streams_poc.avro.domain.Token) defaultValue(fields()[0]);
        }
        record.ApprovalDetails = fieldSetFlags()[1] ? this.ApprovalDetails : (java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail>) defaultValue(fields()[1]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<ApprovalDetails>
    WRITER$ = (org.apache.avro.io.DatumWriter<ApprovalDetails>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<ApprovalDetails>
    READER$ = (org.apache.avro.io.DatumReader<ApprovalDetails>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    this.token.customEncode(out);

    long size0 = this.ApprovalDetails.size();
    out.writeArrayStart();
    out.setItemCount(size0);
    long actualSize0 = 0;
    for (sean.kafka_streams_poc.avro.domain.ApprovalDetail e0: this.ApprovalDetails) {
      actualSize0++;
      out.startItem();
      e0.customEncode(out);
    }
    out.writeArrayEnd();
    if (actualSize0 != size0)
      throw new java.util.ConcurrentModificationException("Array-size written was " + size0 + ", but element count was " + actualSize0 + ".");

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      if (this.token == null) {
        this.token = new sean.kafka_streams_poc.avro.domain.Token();
      }
      this.token.customDecode(in);

      long size0 = in.readArrayStart();
      java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> a0 = this.ApprovalDetails;
      if (a0 == null) {
        a0 = new SpecificData.Array<sean.kafka_streams_poc.avro.domain.ApprovalDetail>((int)size0, SCHEMA$.getField("ApprovalDetails").schema());
        this.ApprovalDetails = a0;
      } else a0.clear();
      SpecificData.Array<sean.kafka_streams_poc.avro.domain.ApprovalDetail> ga0 = (a0 instanceof SpecificData.Array ? (SpecificData.Array<sean.kafka_streams_poc.avro.domain.ApprovalDetail>)a0 : null);
      for ( ; 0 < size0; size0 = in.arrayNext()) {
        for ( ; size0 != 0; size0--) {
          sean.kafka_streams_poc.avro.domain.ApprovalDetail e0 = (ga0 != null ? ga0.peek() : null);
          if (e0 == null) {
            e0 = new sean.kafka_streams_poc.avro.domain.ApprovalDetail();
          }
          e0.customDecode(in);
          a0.add(e0);
        }
      }

    } else {
      for (int i = 0; i < 2; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          if (this.token == null) {
            this.token = new sean.kafka_streams_poc.avro.domain.Token();
          }
          this.token.customDecode(in);
          break;

        case 1:
          long size0 = in.readArrayStart();
          java.util.List<sean.kafka_streams_poc.avro.domain.ApprovalDetail> a0 = this.ApprovalDetails;
          if (a0 == null) {
            a0 = new SpecificData.Array<sean.kafka_streams_poc.avro.domain.ApprovalDetail>((int)size0, SCHEMA$.getField("ApprovalDetails").schema());
            this.ApprovalDetails = a0;
          } else a0.clear();
          SpecificData.Array<sean.kafka_streams_poc.avro.domain.ApprovalDetail> ga0 = (a0 instanceof SpecificData.Array ? (SpecificData.Array<sean.kafka_streams_poc.avro.domain.ApprovalDetail>)a0 : null);
          for ( ; 0 < size0; size0 = in.arrayNext()) {
            for ( ; size0 != 0; size0--) {
              sean.kafka_streams_poc.avro.domain.ApprovalDetail e0 = (ga0 != null ? ga0.peek() : null);
              if (e0 == null) {
                e0 = new sean.kafka_streams_poc.avro.domain.ApprovalDetail();
              }
              e0.customDecode(in);
              a0.add(e0);
            }
          }
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










