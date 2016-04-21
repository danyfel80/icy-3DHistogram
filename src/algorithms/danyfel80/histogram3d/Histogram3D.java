package algorithms.danyfel80.histogram3d;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.TypeUtil;

/**
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class Histogram3D {

  private Sequence   seq;
  private Sequence   count;
  private Sequence   colorHisto;

  private int[][]    countData;
  private byte[][][] colorData;

  /**
   * Constructor using an RGB UBYTE sequence as input.
   */
  public Histogram3D(Sequence source) {
    seq = source;
  }

  public Sequence computeHistogram() {
    int sx = seq.getSizeX(), sy = seq.getSizeY(), sz = seq.getSizeZ(), x, y, z,
        sxh = 256, syh = 256, szh = 256, xh, yh, zh;
    count = new Sequence(seq.getName() + "Histogram");
    count.beginUpdate();
    try {
      for (z = 0; z < szh; z++) {
        count.setImage(0, z, new IcyBufferedImage(sxh, syh, 1, DataType.UINT));
      }
    }
    finally {
      count.endUpdate();
    }

    count.beginUpdate();
    try {
      countData = count.getDataXYZAsInt(0, 0);
      byte[][][] seqData = seq.getDataXYCZAsByte(0);

      for (z = 0; z < sz; z++) {
        for (y = 0; y < sy; y++) {
          for (x = 0; x < sx; x++) {
            xh = TypeUtil.unsign(seqData[z][0][x + y * sx]);
            yh = TypeUtil.unsign(seqData[z][1][x + y * sx]);
            zh = TypeUtil.unsign(seqData[z][2][x + y * sx]);
            countData[zh][xh + yh * sxh]++;
          }
        }
      }
      count.dataChanged();
    }
    finally {
      count.endUpdate();
    }
    return count;
  }

  /**
   * @return Sequence with the colors present in the sequence.
   */
  public Sequence getColoredHistogram(boolean logarithmic) {
    if (count == null) {
      computeHistogram();
    }

    int sx = count.getSizeX(), sy = count.getSizeY(), sz = count.getSizeZ(), x,
        y, z;
    long val;

    this.colorHisto = new Sequence(seq.getName() + "ColorHistogram");
    colorHisto.beginUpdate();
    try {
      for (z = 0; z < sz; z++) {
        IcyBufferedImage im = new IcyBufferedImage(sx, sy, 3, DataType.UBYTE);
        colorHisto.setImage(0, z, im);
      }
    }
    finally {
      colorHisto.endUpdate();
    }

    colorHisto.beginUpdate();
    try {
      countData = count.getDataXYZAsInt(0, 0);
      colorData = colorHisto.getDataXYCZAsByte(0);

      for (z = 0; z < sz; z++) {
        for (y = 0; y < sy; y++) {
          for (x = 0; x < sx; x++) {
            val = TypeUtil.unsign(countData[z][x + y * sx]);
            if (val > 0) {
              colorData[z][0][x + y * sx] = (byte) x;
              colorData[z][1][x + y * sx] = (byte) y;
              colorData[z][2][x + y * sx] = (byte) z;
            }
          }
        }
      }
      colorHisto.dataChanged();
    }
    finally {
      colorHisto.endUpdate();
    }
    return colorHisto;

  }

  public double getProba(int r, int g, int b, int neighborhoodSize) {
    int x, y, z, sx = count.getSizeX(), sy = count.getSizeY(),
        sz = count.getSizeZ(), numVox = 0;
    double proba = 0;
    for (x = r - neighborhoodSize; x < r + neighborhoodSize; x++) {
      for (y = g - neighborhoodSize; y < g + neighborhoodSize; y++) {
        for (z = b - neighborhoodSize; z < b + neighborhoodSize; z++) {
          if (x >= 0 && x < sx && y >= 0 && y < sy && z >= 0 && z < sz) {
            proba += countData[z][x + y * sx];
            numVox++;
          }
        }
      }
    }

    return proba / (double) numVox;
  }

}
