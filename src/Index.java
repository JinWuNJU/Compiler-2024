import java.util.Objects;

public class Index {
    int intValue;
    int line;

    public Index(int intValue, int line) {
        this.intValue = intValue;
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return intValue == index.intValue && line == index.line;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intValue, line);
    }
}
