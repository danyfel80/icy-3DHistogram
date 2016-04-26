package plugins.danyfel80.histogram3d;

import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarInteger;
import plugins.adufour.ezplug.EzVarSequence;
import plugins.adufour.vars.lang.VarSequence;
import icy.gui.dialog.MessageDialog;
import icy.image.IcyBufferedImage;
import icy.painter.Overlay;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.profile.CPUMonitor;
import icy.type.DataType;

public class Histogram3D extends EzPlug implements Block {

	private EzVarSequence inSequence;
	private EzVarInteger inBinSideSize;

	private VarSequence outCountHistogram;
	private VarSequence outColorHistogram;

	private Sequence histoSeq;
	private Sequence colorHistoSeq;

	private Sequence blankSeq;

	@Override
	protected void initialize() {
		inSequence = new EzVarSequence("Input Sequence");
		inBinSideSize = new EzVarInteger("Amount of spheres");

		addEzComponent(inSequence);
		addEzComponent(inBinSideSize);
		inBinSideSize.setValue(10);
		inBinSideSize.setMinValue(5);
		inBinSideSize.setMaxValue(15);
	}

	@Override
	protected void execute() {
		if (validateInput() != 0) {
			return;
		}
		Sequence seq = inSequence.getValue();
		if (seq.getDataType_() != DataType.UBYTE) {
			seq = SequenceUtil.convertToType(seq, DataType.UBYTE, true);
		}
		CPUMonitor cpu = new CPUMonitor();
		cpu.start();
		algorithms.danyfel80.histogram3d.Histogram3D histo = new algorithms.danyfel80.histogram3d.Histogram3D(seq);
		histoSeq = histo.computeHistogram();
		colorHistoSeq = histo.getColoredHistogram(false);

		blankSeq = new Sequence("Histogram");
		blankSeq.beginUpdate();
		for (int i = 0; i < 256; i++) {
			blankSeq.setImage(0, i, new IcyBufferedImage(256, 256, 1, DataType.BYTE));
		}
		Overlay weightedHisto = new Histogram3DWeightedOverlay(histoSeq);
		Overlay sphereHisto = new Histogram3DSphereOverlay(histoSeq, inBinSideSize.getValue());
		blankSeq.addOverlay(weightedHisto);
		blankSeq.addOverlay(sphereHisto);

		cpu.stop();
		addSequence(histoSeq);
		addSequence(colorHistoSeq);
		addSequence(blankSeq);
		System.out.println("Histogram 3D computed in " + cpu.getElapsedTimeMilli() + " msec");
	}

	private int validateInput() {
		if (inSequence.getValue() == null || inSequence.getValue().isEmpty()) {
			MessageDialog.showDialog("Invalid input", "Please choose a non-empty sequence.", MessageDialog.ERROR_MESSAGE);
			return -1;
		}
		if (inSequence.getValue().getSizeC() > 3) {
			MessageDialog.showDialog("Invalid input", "Please choose sequence with at least 3 channels.",
			    MessageDialog.ERROR_MESSAGE);
			return -2;
		}
		return 0;
	}

	@Override
	public void clean() {
	}

	@Override
	public void declareInput(VarList inputMap) {
		inSequence = new EzVarSequence("Input Sequence");

		inputMap.add(inSequence.name, inSequence.getVariable());
	}

	@Override
	public void declareOutput(VarList outputMap) {
		this.outCountHistogram = new VarSequence("CountHistogram", histoSeq);
		this.outColorHistogram = new VarSequence("ColorHistogram", colorHistoSeq);
		outputMap.add(outCountHistogram.getName(), outCountHistogram);
		outputMap.add(outColorHistogram.getName(), outColorHistogram);
	}
}
