package store.bookscamp.api.member.entity;

public enum MemberStatus {
    NORMAL,
    DORMANT,
    WITHDRAWN;

    public static MemberStatus from(String value){
        for(MemberStatus status : MemberStatus.values()){
            if(status.name().equalsIgnoreCase(value)){
                return status;
            }
        }
        return null;
    }
}

