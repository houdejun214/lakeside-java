#This is the thrift interface file for Hajo
namespace java com.hajo.thrift
#namespace cpp com.hajo.thrift
#namespace csharp Hajo
#namespace py hajo
#namespace php hajo
#namespace perl hajo
#namespace rb hajo


const string VERSION = "0.0.1"

struct RecordType{
    1: binary key,
    2: binary value,
}

exception HajoException{
    1:string message
}

service HajoService{
    void insertRecord(1:RecordType record) throws (1:HajoException e)
    RecordType getRecord(1:binary key) throws (1:HajoException e)
    void deleteRecord(1:binary key) throws (1:HajoException e)
}