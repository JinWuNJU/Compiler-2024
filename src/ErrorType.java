public enum ErrorType {
    Redefinde_function(4),Undefined_variable(1),Undefined_function(2)
    ,Redefined_variable(3),Type_mismatched_for_assignment(5),Type_mismatched_for_operands(6),
    Type_mismatched_for_return(7),Function_is_not_applicable_for_arguments(8),Not_an_array(9),
    Not_a_function(10),The_left_hand_side_of_an_assignment_must_be_a_variable(11);
    private int type;
    ErrorType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
