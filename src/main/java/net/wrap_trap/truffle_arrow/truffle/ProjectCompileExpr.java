package net.wrap_trap.truffle_arrow.truffle;

import com.oracle.truffle.api.frame.FrameSlot;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;

import java.util.Objects;

public class ProjectCompileExpr extends CompileExpr {

  public static ExprBase compile(FrameDescriptorPart from, RexNode child, SinkContext context) {
    CompileExpr compiler = new ProjectCompileExpr(from, context);
    return child.accept(compiler);
  }

  ProjectCompileExpr(FrameDescriptorPart from, SinkContext context) {
    super(from, context);
  }

  @Override
  public ExprBase visitInputRef(RexInputRef inputRef) {
    int index = inputRef.getIndex();
    FrameSlot slot = from.findFrameSlotInPrevious(index);
    Objects.requireNonNull(slot);

    return ExprReadLocalNodeGen.create(slot);
  }

  @Override
  protected CompileExpr createCompileExpr(FrameDescriptorPart from, SinkContext context) {
    return new ProjectCompileExpr(from, context);
  }
}