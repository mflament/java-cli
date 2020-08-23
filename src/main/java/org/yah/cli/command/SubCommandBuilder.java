package org.yah.cli.command;

public class SubCommandBuilder<P extends AbstractCommandBuilder<P>>
        extends AbstractCommandBuilder<SubCommandBuilder<P>> {
    private final P parent;

    SubCommandBuilder(P parent, String name) {
        super(name, parent.isStrict());
        this.parent = parent;
    }

    public SubCommandBuilder<SubCommandBuilder<P>> withSubCommand(String name) {
        return new SubCommandBuilder<>(this, name);
    }

    public P build() {
        prepare();
        return parent.withSubCommands(new Command(this));
    }

    @Override
    protected SubCommandBuilder<P> getThis() {
        return this;
    }
}
