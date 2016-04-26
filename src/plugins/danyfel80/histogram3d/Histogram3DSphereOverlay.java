/**
 * 
 */
package plugins.danyfel80.histogram3d;

import icy.painter.Overlay;
import icy.painter.VtkPainter;
import icy.sequence.Sequence;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkSphereSource;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class Histogram3DSphereOverlay extends Overlay implements VtkPainter {

	vtkProp[] spheres;

	public Histogram3DSphereOverlay(Sequence counts, int numBins) {
		super("Sphere Histogram");
		init(counts, numBins);
	}

	private void init(Sequence counts, int numBins) {

		int x, y, z, binIdx = 0, maxSum;
		double binSize = 256.0 / (double) numBins;
		int numTotalBins = numBins * numBins * numBins;
		int[] sums = new int[numTotalBins];
		int[][] countsData = counts.getDataXYZAsInt(0, 0);

		vtkActor[] actors = new vtkActor[numTotalBins];

		for (z = 0; z < counts.getSizeZ(); z++) {
			for (y = 0; y < counts.getSizeY(); y++) {
				for (x = 0; x < counts.getSizeX(); x++) {
					binIdx = (int) (x / binSize) + (int) (y / binSize) * numBins + (int) (z / binSize) * numBins * numBins;
					sums[binIdx] += countsData[z][x + y * counts.getSizeX()];
				}
			}
		}

		maxSum = 0;
		for (int i = 0; i < numTotalBins; i++) {
			maxSum = Math.max(maxSum, sums[i]);
		}

		int numActors = 0, val;
		int cX, cY, cZ;
		double r = 0;
		for (z = 0; z < numBins; z++) {
			cZ = (int) (binSize / 2) + (int) (z * binSize);
			for (y = 0; y < numBins; y++) {
				cY = (int) (binSize / 2) + (int) (y * binSize);
				for (x = 0; x < numBins; x++) {
					cX = (int) (binSize / 2) + (int) (x * binSize);
					
					val = sums[z * numBins * numBins + y * numBins + x];
					if (val > 0) {
						vtkSphereSource spSrc = new vtkSphereSource();
						vtkPolyDataMapper spPDM = new vtkPolyDataMapper();
						vtkActor spActor = new vtkActor();
						
						spPDM.SetInputConnection(spSrc.GetOutputPort());
						spActor.SetMapper(spPDM);

						r = (double)val / (double)maxSum;
						//r = Math.log(val) / Math.log(maxSum);
						spSrc.SetCenter(new double[] { cX, cY, cZ });
						spSrc.SetRadius(r * binSize / 2.0);
						final double colR = (double)cX/256.0, colG = (double)cY/256.0, colB = (double)cZ/256.0;
						spActor.GetProperty().SetColor(colR, colG, colB);

						actors[numActors++] = spActor;
					}
				}
			}
		}

		spheres = actors;
	}

	@Override
	public vtkProp[] getProps() {
		return spheres;
	}

}
