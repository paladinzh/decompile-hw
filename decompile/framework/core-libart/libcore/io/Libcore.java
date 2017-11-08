package libcore.io;

public final class Libcore {
    public static Os os = new BlockGuardOs(new Posix());

    private Libcore() {
    }
}
