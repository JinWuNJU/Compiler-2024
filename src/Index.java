import java.util.Objects;

public class Index {
    int intValue;
    String stringValue;

    public Index(int intValue, String stringValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intValue, stringValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Index index = (Index) obj;
        return intValue == index.intValue && Objects.equals(stringValue, index.stringValue);
    }
}
