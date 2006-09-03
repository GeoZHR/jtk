/****************************************************************************
Copyright (c) 2004, Landmark Graphics and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.opt;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import edu.mines.jtk.util.Almost;

/** Implements a Vect by wrapping an array of floats.
    The embedded data are exposed by a getData method.  For all practical
    purposes this member is public, except that this class must always
    point to the same array.  The implementation as an array
    is the point of this class, to avoid duplicate implementations
    elsewhere.  Multiple inheritance is prohibited and
    prevents the mixin pattern, but you can share the wrapped array
    as a private member of your own class,
    and easily delegate all implemented methods.
    @author W.S. Harlan
 */
public class ArrayVect1f implements Vect {
  private static final long serialVersionUID = 1L;

  /** Array of wrapped data */
  protected float[] _data = null;

  /** Variance of each ArrayVect1f */
  protected double _variance = 1.;

  /** This is the first sample to treat as non-zero.
      Earlier samples should be constrained to zero. */
  protected int _firstSample = 0;

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger("edu.mines.jtk.opt");

  /** Construct from an array of data.
      @param data This is the data that will be manipulated.
      @param firstSample This is the first sample to treat as non-zero.
      Earlier samples should be constrained to zero.
      @param variance The method multiplyInverseCovariance()
      will divide all samples by this number.  Pass a value
      of 1 if you do not care.
  */
  public ArrayVect1f(float[] data, int firstSample, double variance) {
    init(data, firstSample, variance);
  }

  /** Constructor for derived classes that call init() */
  protected ArrayVect1f() {}

  /** Construct from an array of data.
      @param data This is the data that will be manipulated.
      @param firstSample This is the first sample to treat as non-zero.
      Earlier samples should be constrained to zero.
      @param variance The method multiplyInverseCovariance()
      will divide all samples by this number.  Pass a value
      of 1 if you do not care.
  */
  protected void init(float[] data, int firstSample, double variance) {
    _data = data;
    _firstSample = firstSample;
    _variance = variance;
  }

  /** This is the first sample to treat as non-zero.
      @return first non-zero sample
   */
  public int getFirstSample() {
    return _firstSample;
  }

  /** Return the size of the embedded array
      @return size of embedded array*/
  public int getSize() {return _data.length;}

  /** Get the embedded data
      @return Same array as passed to constructor.
   */
  public float[] getData() {
    return _data;
  }

  /** Set the internal data array to new values.
      @param data Copy this data into the internal wrapped array.
   */
  public void setData(float[] data) {
    System.arraycopy(data,0, _data, 0, _data.length);
  }

  @Override public ArrayVect1f clone() {
    try {
      ArrayVect1f result = (ArrayVect1f) super.clone();
      if (_data != null) {
        float[] newData = _data.clone();
        result.init(newData, _firstSample, _variance);
      } // else being used by a class factory
      return result;
    } catch (CloneNotSupportedException ex) {
      IllegalStateException e = new IllegalStateException(ex.getMessage());
      e.initCause(ex);
      throw e;
    }
  }

  public double dot(VectConst other) {
    double result = 0;
    ArrayVect1f rhs = (ArrayVect1f) other;
    for (int i=0; /*Math.max(_firstSample,rhs._firstSample)//breaks transpose*/
         i<_data.length;
         ++i) {
      result += _data[i] * rhs._data[i];
    }
    return result;
  }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i=0; i<_data.length; ++i) {
      sb.append(""+_data[i]);
      if (i < _data.length -1) {sb.append(", ");}
    }
    sb.append(")");
    return sb.toString();
  }

  public void dispose() {
    _data = null;
  }

  public void multiplyInverseCovariance() {
    double scale = Almost.FLOAT.divide (1., getSize()*_variance, 0.);
    VectUtil.scale(this, scale);
  }

  public void constrain() {
    Arrays.fill(_data, 0, _firstSample, 0.f); // remute
  }

  public void add(double scaleThis, double scaleOther, VectConst other)  {
    float s1 = (float) scaleThis;
    float s2 = (float) scaleOther;
    ArrayVect1f rhs = (ArrayVect1f) other;
    for (int i=0; i<_data.length; ++i) {
      _data[i] = s1*_data[i] + s2*rhs._data[i];
    }
  }

  public void project(double scaleThis, double scaleOther, VectConst other)  {
    add(scaleThis, scaleOther, other);
  }

  public double magnitude() {
    return Almost.FLOAT.divide (this.dot(this), getSize()*_variance, 0.);
  }

  // Vect
  public void postCondition() {}

  // Serializable
  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    out.writeObject(_data);
    out.writeDouble(_variance);
    out.writeInt(_firstSample);
  }

  // Serializable
  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    _data = (float[]) in.readObject();
    _variance =  in.readDouble();
    _firstSample = in.readInt();
  }
}

