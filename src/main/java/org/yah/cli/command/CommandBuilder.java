package org.yah.cli.command;

import java.util.Collection;

public class CommandBuilder extends AbstractCommandBuilder<CommandBuilder> {

    CommandBuilder(String name, boolean strict) {
        super(name, strict);
    }

    public Command build() {
        prepare();
        return new Command(this);
    }

    public void addTo(Collection<Command> commands) {
        commands.add(build());
    }

    public void addTo(CommandsRegistry registry) {
        registry.merge(build());
    }

    public SubCommandBuilder<CommandBuilder> withSubCommand(String name) {
        return new SubCommandBuilder<>(this, name);
    }

    @Override
    protected CommandBuilder getThis() {
        return this;
    }

}
