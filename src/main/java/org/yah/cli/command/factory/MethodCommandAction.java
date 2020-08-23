package org.yah.cli.command.factory;

import org.yah.cli.command.BuilderActions.ParsedParametersCommandAction;
import org.yah.cli.command.Command;
import org.yah.cli.command.parameter.ParsedParameter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


class MethodCommandAction implements ParsedParametersCommandAction {

    private final Object instance;
    private final Method method;

    private final ParameterSupplier[] parametersSuppliers;

    MethodCommandAction(Object instance, Method method, ParameterSupplier[] parametersSuppliers) {
        if (parametersSuppliers.length != method.getParameterCount())
            throw new IllegalStateException("parameters suppliers and method parameters count mismatch");
        this.instance = instance;
        this.method = method;
        if (!method.canAccess(instance))
            method.setAccessible(true);
        this.parametersSuppliers = parametersSuppliers;
    }

    @Override
    public void run(Command command, List<ParsedParameter> parameters) throws Exception {
        Object[] params = new Object[parametersSuppliers.length];
        for (int i = 0; i < parametersSuppliers.length; i++) {
            params[i] = parametersSuppliers[i].get(command, parameters);
        }
        try {
            method.invoke(instance, params);
        } catch (InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if(targetException instanceof Exception)
                throw (Exception)targetException;
            throw e;
        }
    }

}
