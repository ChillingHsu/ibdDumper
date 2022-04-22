package formatter;

import java.util.function.Consumer;
import parser.Layout;

public interface Formatter {
    void child();

    void parent();

    void item(String content);

    void append(String content);

    void end();

    default <T extends Layout & Referable<?, R>, R extends Layout> R deref(
            T ref, Class<R> targetClass) {
        return (R) ref;
    }

    default <T extends Layout & Referable<?, R>, R extends Layout> void derefItem(
            T ref, Class<R> targetClass) {
        R derefed = this.<T, R>deref(ref, targetClass);
        if (derefed == null) return;
        derefed.format(this, true);
    }

    default void subItem(Consumer<Formatter> content) {
        this.child();
        content.accept(this);
        this.parent();
    }
}
