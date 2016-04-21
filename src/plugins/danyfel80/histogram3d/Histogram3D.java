package plugins.danyfel80.histogram3d;

import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarSequence;
import icy.gui.dialog.MessageDialog;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.profile.CPUMonitor;
import icy.type.DataType;

public class Histogram3D extends EzPlug implements Block {

  private EzVarSequence inSequence;

  @Override
  protected void initialize() {
    inSequence = new EzVarSequence("Input Sequence");
    addEzComponent(inSequence);
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
    algorithms.danyfel80.histogram3d.Histogram3D histo =
        new algorithms.danyfel80.histogram3d.Histogram3D(seq);
    Sequence histoSeq = histo.computeHistogram();
    Sequence colorHistoSeq = histo.getColoredHistogram(false);
    cpu.stop();
    addSequence(histoSeq);
    addSequence(colorHistoSeq);
    System.out.println(
        "Histogram 3D computed in " + cpu.getElapsedTimeMilli() + " msec");
  }

  private int validateInput() {
    if (inSequence.getValue() == null || inSequence.getValue().isEmpty()) {
      MessageDialog.showDialog("Invalid input",
          "Please choose a non-empty sequence.", MessageDialog.ERROR_MESSAGE);
      return -1;
    }
    if (inSequence.getValue().getSizeC() > 3) {
      MessageDialog.showDialog("Invalid input",
          "Please choose sequence with at least 3 channels.",
          MessageDialog.ERROR_MESSAGE);
      return -2;
    }
    return 0;
  }

  @Override
  public void clean() {}

  @Override
  public void declareInput(VarList inputMap) {
    inSequence = new EzVarSequence("Input Sequence");

    inputMap.add(inSequence.name, inSequence.getVariable());
  }

  @Override
  public void declareOutput(VarList outputMap) {

  }
}
