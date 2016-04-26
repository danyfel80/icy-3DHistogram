package plugins.danyfel80.histogram3d;

import icy.painter.Overlay;
import icy.painter.VtkPainter;
import icy.sequence.Sequence;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

/**
 * 3D count
 * 
 * @author Daniel
 */
public class Histogram3DWeightedOverlay extends Overlay implements VtkPainter {

	private vtkActor pointsActor;

	/**
	 * @param name
	 */
	public Histogram3DWeightedOverlay(Sequence counts) {
		super("Weighted Histogram");
		init(counts);
	}

	private void init(Sequence counts) {
		vtkPoints pts = new vtkPoints();
		vtkCellArray ptsArr = new vtkCellArray();
		vtkPolyData pData = new vtkPolyData();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();

		pData.SetPoints(pts);
		pData.SetVerts(ptsArr);
		colors.SetNumberOfComponents(4);
		colors.SetName("Colors");

		int maxVal = (int) counts.getChannelMax(0);
		int[][] countsData = counts.getDataXYZAsInt(0, 0);
		for (int z = 0; z < counts.getSizeZ(); z++) {
			for (int y = 0; y < counts.getSizeY(); y++) {
				for (int x = 0; x < counts.getSizeX(); x++) {
					int val = countsData[z][y * counts.getSizeX() + x];
					if (val > 0) {
						int ptID = pts.InsertNextPoint(x, y, z);
						colors.InsertNextTuple4(x, y, z, (int) (Math.log((double) val) / Math.log(maxVal) * 255.0));
						ptsArr.InsertNextCell(ptID);
					}
				}
			}
		}

		pData.GetPointData().SetScalars(colors);
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		pointsActor = new vtkActor();
		pointsActor.GetProperty().SetOpacity(0.9999);
		mapper.SetInputData(pData);
		pointsActor.SetMapper(mapper);
	}

	@Override
	public vtkProp[] getProps() {
		return new vtkProp[] { pointsActor };
	}

}
