package net.wrap_trap.truffle_arrow.truffle;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import net.wrap_trap.truffle_arrow.ArrowFieldType;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.UInt4Vector;
import org.apache.calcite.rex.RexNode;

import java.util.List;


public class FilterSink extends RowSink {

  public static FilterSink createSink(
    FrameDescriptor frameDescriptor,
    RexNode condition,
    SinkContext context,
    ThenRowSink next) {
    RowSink rowSink = next.apply(frameDescriptor);
    return new FilterSink(CompileExpr.compile(frameDescriptor, condition, context), rowSink);
  }

  RowSink then;
  ExprBase conditionExpr;

  private FilterSink(ExprBase conditionExpr, RowSink then) {
    this.conditionExpr = conditionExpr;
    this.then = then;
  }

  @Override
  public void executeVoid(VirtualFrame frame, FrameDescriptor frameDescriptor, SinkContext context) throws UnexpectedResultException {
    List<Integer> indices = context.getInputRefIndices();
    List<FieldVector> vectors = context.vectors();
    UInt4Vector selectionVector = context.selectionVector();
    selectionVector.setValueCount(vectors.get(0).getValueCount());
    int s = 0;

    for (int i = 0; i < vectors.get(0).getValueCount(); i ++) {
      for (int j = 0; j < indices.size(); j ++) {
        int index = indices.get(j);
        FrameSlot slot = frameDescriptor.findFrameSlot(index);
        ArrowFieldType type = ArrowFieldType.of(vectors.get(j).getField().getFieldType().getType());

        switch (type) {
          case INT:
            frame.setInt(slot, (int) vectors.get(j).getObject(i));
            break;
          case LONG:
            frame.setLong(slot, (long) vectors.get(j).getObject(i));
            break;
          case DOUBLE:
            frame.setDouble(slot, (double) vectors.get(j).getObject(i));
            break;
          case TIMESTAMP:
          case TIME:
          case DATE:
          case STRING:
            frame.setObject(slot, vectors.get(j).getObject(i));
            break;
          default:
            throw new IllegalArgumentException("Unexpected ArrowFieldType:" + type);
        }
      }
      if (this.conditionExpr.executeBoolean(frame)) {
        selectionVector.set(s ++, i);
      }
    }
    selectionVector.setValueCount(s);

    then.executeVoid(frame, frameDescriptor, context);
  }
}
