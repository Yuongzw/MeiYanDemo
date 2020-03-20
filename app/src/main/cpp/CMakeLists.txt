
cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_STANDARD 14)

#导入头文件
include_directories("include")

file(GLOB my_source_path ${CMAKE_SOURCE_DIR}/*.cpp ${CMAKE_SOURCE_DIR}/*.c)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../../../libs/${ANDROID_ABI}")

#add_library(lib_opencv SHARED IMPORTED)
#set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI}/libopencv_java4.so)
#message(${CMAKE_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI}/libopencv_java4.so)
add_library(
        native-lib

        SHARED
        ${my_source_path}
        #        native-lib.cpp
)

find_library(
        log-lib

        log)

add_subdirectory(FaceAlignment)
include_directories(FaceAlignment/include)

target_link_libraries(
        native-lib
        opencv_java4
        ${log-lib}
        android
        seeta_fa_lib)