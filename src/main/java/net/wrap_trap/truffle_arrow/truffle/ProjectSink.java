package net.wrap_trap.truffle_arrow.truffle;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.apache.calcite.rex.RexNode;

import java.util.List;


public class ProjectSink extends RelRowSink {

  public static ProjectSink createSink(
    FrameDescriptorPart framePart,
    List<? extends RexNode> projects,
    CompileContext compileContext,
    ThenRowSink next) {
    FrameDescriptorPart newFramePart = framePart.newPart();
    for (int i = 0; i < projects.size(); i ++) {
      newFramePart.addFrameSlot();
    }
    RowSink rowSink = next.apply(newFramePart);

    StatementWriteLocal[] locals = new StatementWriteLocal[projects.size()];
    for (int i = 0; i < projects.size(); i ++) {
      RexNode child = projects.get(i);
      ExprBase compiled = compile(newFramePart, child, compileContext);
      FrameSlot slot = newFramePart.findFrameSlot(i);
      locals[i] = StatementWriteLocalNodeGen.create(compiled, slot);
    }
    return new ProjectSink(newFramePart, locals, rowSink);
  }

  private static ExprBase compile(FrameDescriptorPart framePart, RexNode child, CompileContext compileContext) {
    return ProjectCompileExpr.compile(framePart, child, compileContext);
  }

  private FrameDescriptorPart framePart;
  private StatementWriteLocal[] locals;

  private ProjectSink(FrameDescriptorPart framePart, StatementWriteLocal[] locals, RowSink then) {
    super(then);
    this.framePart = framePart;
    this.locals = locals;
  }

  @Override
  public void executeByRow(VirtualFrame frame, FrameDescriptorPart framePart, SinkContext context) throws UnexpectedResultException {
    for (StatementWriteLocal local : locals) {
      local.executeVoid(frame);
    }
    then.executeByRow(frame, this.framePart, context);
  }
}