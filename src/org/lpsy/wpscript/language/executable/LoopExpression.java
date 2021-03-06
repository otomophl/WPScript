package org.lpsy.wpscript.language.executable;

import javax.swing.JOptionPane;
import org.lpsy.wpscript.language.exceptions.CompilationErrorException;
import org.lpsy.wpscript.language.exceptions.PanicException;
import org.lpsy.wpscript.language.ScriptParser;
import org.lpsy.wpscript.language.exceptions.RuntimeErrorException;
import org.lpsy.wpscript.language.executable.builtintypes.Bool;
import org.lpsy.wpscript.language.memory.Environment;

/**
 * @author Laurent FABRE, 2011-2015
 */
public class LoopExpression extends Calculable {
    Expression pre_calculation;
    Expression increment_calculation;
    Calculable condition;
    Expression calculation;
    //FOR LOOP
    public LoopExpression(ScriptParser _interpreter, Expression _pre_calculation, Expression _increment_calculation, Calculable _condition, Expression _expression) {
        _init(_interpreter, _pre_calculation, _increment_calculation, _condition, _expression);
        line_number = interpreter.getLineNumber();
    }
    //WHILE LOOP
    public LoopExpression(ScriptParser _interpreter, Calculable _condition, Expression _expression) {
        _init(_interpreter, null, null, _condition, _expression);
        line_number = interpreter.getLineNumber();
    }

    public LoopExpression(ScriptParser _interpreter, int _line_number, Expression _pre_calculation, Expression _increment_calculation, Calculable _condition, Expression _expression) {
        _init(_interpreter, _pre_calculation, _increment_calculation, _condition, _expression);
        line_number = _line_number;
    }
    private void _init(ScriptParser _interpreter, Expression _pre_calculation, Expression _increment_calculation, Calculable _condition, Expression _calculation) {
        pre_calculation = _pre_calculation;
        increment_calculation = _increment_calculation;
        interpreter = _interpreter;
         condition = _condition;
         calculation = _calculation;
    }

    @Override
    public void setEnv(Environment _env) {
	env = _env;
	pre_calculation.setEnv(_env);
	increment_calculation.setEnv(_env);
	condition.setEnv(_env);
	calculation.setEnv(_env);

    }

    @Override
    public void compilationCheck() throws CompilationErrorException, PanicException {
        if (pre_calculation != null) {
            pre_calculation.compilationCheck();
        }
        if (increment_calculation != null) {
            increment_calculation.compilationCheck();
        }
        condition.compilationCheck();
        if (calculation != null) {
            calculation.compilationCheck();
        }
    }

    @Override
    public String toString() {
        if (pre_calculation==null && increment_calculation==null) {
            return "LOOP expression:: WHILE[" + condition.toString() + "] {" + (calculation!=null?calculation.toString():"null") + "}";
        } else {
            return "LOOP expression:: FOR[" + (pre_calculation==null ? "null":pre_calculation.toString()) + " ; " + (increment_calculation==null ? "null":increment_calculation.toString() + " ; " + condition.toString()) + "] {" + (calculation!=null?calculation.toString():"null") + "}";
        }
    }

    public static final int _INFINITE_LOOP_NUMBER_ = 20;

    @Override
    public Object eval() throws PanicException, RuntimeErrorException {
        if (pre_calculation != null) {
            pre_calculation.eval();
        }
        int cnt = 0;
        while (true) {
            Object cond = condition.eval();
            if (!(cond instanceof Bool)) {
                interpreter.runtimeError("Condition must be an instance of BOOL [" + cond.getClass() + "]", line_number);
                return null;
            }
            Object ret_val = ReturnValue.RETURN_NULL;
            if ((Boolean)((Bool)cond).getNativeValue()) {
                if (calculation!=null) {
                    ret_val = calculation.eval();
                    if (ret_val == ReturnValue.RETURN_BREAK) {
                        System.err.println("BREAK!");
                        return ReturnValue.RETURN_BREAK;
                    }
                }
                if (increment_calculation != null) {
                    increment_calculation.eval();
                }
            } else {
                return ret_val;
            }
            cnt++;
            if (cnt >= _INFINITE_LOOP_NUMBER_) {
                cnt = 0;
                int answer = JOptionPane.showConfirmDialog(null, "May be inside infinite loop! Continue?", "Warning", JOptionPane.WARNING_MESSAGE);
                if (answer != JOptionPane.OK_OPTION) {
                    interpreter.runtimeError("Infinite loop: execution aborted!", line_number);
                    return ReturnValue.RETURN_NULL;
                }
            }
        }
    }

    @Override
    public Calculable getSimplifiedCalculable() throws PanicException {
        Expression new_pre_calculation = (Expression) (pre_calculation==null ? null : pre_calculation.getSimplifiedCalculable());
        Expression new_increment_calculation = (Expression) (increment_calculation==null ? null : increment_calculation.getSimplifiedCalculable());
        Calculable new_condition = (Calculable) condition.getSimplifiedCalculable();
        Expression new_calculation = (Expression) calculation.getSimplifiedCalculable();
        return new LoopExpression(interpreter, line_number, new_pre_calculation, new_increment_calculation, new_condition, new_calculation);
    }

}
