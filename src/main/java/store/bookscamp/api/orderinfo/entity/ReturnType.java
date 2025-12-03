package store.bookscamp.api.orderinfo.entity;

public enum ReturnType {

    CHANGE_OF_MIND(10), // 단순 변심
    DAMAGED_OR_DEFECTIVE(30); // 파손, 파본

    private final int allowableDays;

    ReturnType(int allowableDays) {
        this.allowableDays = allowableDays;
    }

    public int getAllowableDays() {
        return allowableDays;
    }
}
