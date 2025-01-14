package semanticanalysis.types;

import semanticanalysis.IScope;

import java.lang.reflect.Method;

public class AggregateTypeAdapter extends AggregateType {

    final Method builderMethod;

    public Method getBuilderMethod() {
        return builderMethod;
    }

    public AggregateTypeAdapter(
            String name, IScope parentScope, Class<?> originType, Method builderMethod) {
        super(name, parentScope, originType);
        this.builderMethod = builderMethod;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.AggregateAdapted;
    }
}
