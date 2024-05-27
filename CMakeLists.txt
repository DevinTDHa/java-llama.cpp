cmake_minimum_required(VERSION 3.12)

project(jllama CXX)

set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(BUILD_SHARED_LIBS ON)

# checkout llama.cpp
include(FetchContent)
FetchContent_Declare(
        llama.cpp
        GIT_REPOSITORY https://github.com/ggerganov/llama.cpp.git
        #	GIT_TAG        b1645
        GIT_TAG b2589
)
FetchContent_MakeAvailable(llama.cpp)

# todo: Is there a better way to build the library than copy & pasting the build argument cmake definition of llama.cpp?
include(build-args.cmake)

# find which OS we build for if not set (make sure to run mvn compile first)
if (NOT DEFINED OS_NAME)
    find_package(Java REQUIRED)
    find_program(JAVA_EXECUTABLE NAMES java)
    execute_process(
            COMMAND ${JAVA_EXECUTABLE} -cp ${CMAKE_SOURCE_DIR}/target/classes de.kherud.llama.OSInfo --os
            OUTPUT_VARIABLE OS_NAME
            OUTPUT_STRIP_TRAILING_WHITESPACE
    )
endif ()
if (NOT OS_NAME)
    message(FATAL_ERROR "Could not determine OS name")
endif ()

# find which architecture we build for if not set  (make sure to run mvn compile first)
if (NOT DEFINED OS_ARCH)
    find_package(Java REQUIRED)
    find_program(JAVA_EXECUTABLE NAMES java)
    execute_process(
            COMMAND ${JAVA_EXECUTABLE} -cp ${CMAKE_SOURCE_DIR}/target/classes de.kherud.llama.OSInfo --arch
            OUTPUT_VARIABLE OS_ARCH
            OUTPUT_STRIP_TRAILING_WHITESPACE
    )
endif ()
if (NOT OS_ARCH)
    message(FATAL_ERROR "Could not determine CPU architecture")
endif ()

set(JLLAMA_DIR ${CMAKE_SOURCE_DIR}/src/main/resources/de/kherud/llama/${OS_NAME}/${OS_ARCH})
message(STATUS "Installing files to ${JLLAMA_DIR}")

add_library(jllama SHARED src/main/cpp/jllama.cpp)
add_library(llama_batch_inference SHARED src/main/cpp/llama_batch_inference.cpp)

# include jni.h and jni_md.h
if (NOT DEFINED JNI_INCLUDE_DIRS)
    if (OS_NAME MATCHES "^Linux" OR OS_NAME STREQUAL "Mac")
        set(JNI_INCLUDE_DIRS .github/include/unix)
    elseif (OS_NAME STREQUAL "Windows")
        set(JNI_INCLUDE_DIRS .github/include/windows)
        # if we don't have provided headers, try to find them via Java
    else ()
        find_package(Java REQUIRED)
        find_program(JAVA_EXECUTABLE NAMES java)

        find_path(JNI_INCLUDE_DIRS NAMES jni.h HINTS ENV JAVA_HOME PATH_SUFFIXES include)

        # find "jni_md.h" include directory if not set
        file(GLOB_RECURSE JNI_MD_PATHS RELATIVE "${JNI_INCLUDE_DIRS}" "${JNI_INCLUDE_DIRS}/**/jni_md.h")
        foreach (PATH IN LISTS JNI_MD_PATHS)
            get_filename_component(DIR ${PATH} DIRECTORY)
            list(APPEND JNI_INCLUDE_DIRS "${JNI_INCLUDE_DIRS}/${DIR}")
        endforeach ()
    endif ()
endif ()

# Detect Java installation and include jni.h. TODO: MOre minimal solution?
# find_package(JNI REQUIRED)

if (NOT JNI_INCLUDE_DIRS)
    message(FATAL_ERROR "Could not determine JNI include directories")
else ()
    message(STATUS "JNI include directories: ${JNI_INCLUDE_DIRS}")
endif ()

target_include_directories(jllama PRIVATE src/main/cpp ${JNI_INCLUDE_DIRS})
target_include_directories(llama_batch_inference PUBLIC src/main/cpp ${JNI_INCLUDE_DIRS})
target_link_libraries(llama_batch_inference PUBLIC common llama ${LLAMA_EXTRA_LIBS})
target_link_libraries(jllama PRIVATE common llama ${LLAMA_EXTRA_LIBS} llama_batch_inference)
target_compile_features(jllama PRIVATE cxx_std_11)

if (OS_NAME STREQUAL "Windows")
    set_target_properties(jllama llama llama_batch_inference PROPERTIES
            RUNTIME_OUTPUT_DIRECTORY_RELEASE ${JLLAMA_DIR}
    )
else ()
    set_target_properties(jllama llama llama_batch_inference PROPERTIES
            LIBRARY_OUTPUT_DIRECTORY ${JLLAMA_DIR}
    )
endif ()

if (LLAMA_METAL)
    # copy ggml-metal.metal to bin directory
    configure_file(${llama.cpp_SOURCE_DIR}/ggml-metal.metal ${JLLAMA_DIR}/ggml-metal.metal COPYONLY)
endif ()

# Test target
add_executable(test_batch src/test/cpp/test_batch.cpp)
# Link the necessary libraries to your test target
target_link_libraries(test_batch PRIVATE llama_batch_inference)
# Set the output directory for the test executable
set_target_properties(test_batch PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
)

# Enable Debug symbols for all libraries if using Debug config
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    message(STATUS "Enabling debug symbols")
    target_compile_options(jllama PRIVATE -g)
    target_compile_options(llama_batch_inference PRIVATE -g)
    target_compile_options(llama PRIVATE -g)
    target_compile_options(test_batch PRIVATE -g)
endif ()