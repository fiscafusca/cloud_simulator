# CS441 - Engineering of Distributed Objects for Cloud Computing: homework 1
### Giorgia Fiscaletti, UIN: 669755907

## Introduction

The purpose of this homework was to build a cloud simulator based on the CloudSim framework, in order to evaluate the execution of application in custom cloud datacenters. The original framework was extended to provide further functionalities, such as a new scheduling policy based on CPU cores and the support for map/reduce tasks.
The project is developed in Scala and can be compiled using SBT. The CloudSim classes that needed further features were extended in Java.

## Instructions

### IntelliJ IDEA

- Open IntelliJ IDEA and select "Check out from Version Control" in the welcome screen
- Select "Git"
- Enter the repository URL (below) and click on "Clone"
```
https://giorgiafiscaletti2@bitbucket.org/giorgiafiscaletti2/giorgia_fiscaletti_hw1.git
```
- Import the sbt project (a log window will appear on the bottom right of the screen) keeping the default settings
- Go in /Giorgia_Fiscaletti_hw1/src/main/scala/com/gfisca2 and run SimulationPolicy.scala
- You will be asked to make a choice between the different configuration files to run the desired simulation: type the number corresponding to the chosen simulation (i.e. type 1.1 for the simulation corresponding to "# of single-core VMs ≥ # of dual-core VMs & # of single-core cloudlets ≥ # of dual-core cloudlets without the scheduling policy")

### SBT CLI

- Open a terminal and type:
```
git clone https://giorgiafiscaletti2@bitbucket.org/giorgiafiscaletti2/giorgia_fiscaletti_hw1.git
```
- To run the tests, type:
```
sbt clean compile test
```
- To run the simulations, type the following command and select the desired simulation when asked:
```
sbt clean compile run
```
- Again, you will be asked to select a configuration file to run the desired simulation, follow the instructions above for IntelliJ IDEA
## Project Structure

The project has been developed both in Scala and Java. The simulations and the tests have been written in Scala, while the extensions of the CloudSim classes are written in Java. A configuration file is provided for each simulation.

### Scala

#### Simulations

In order to avoid code redundancy, only one file is provided: SimulationPolicy.scala. It allows to run eight distinct simulations with four different configurations (i.e. number of single-core VMs, dual-core VMs, single-core cloudlets and dual-core cloudlets), all of them with or without the scheduling policy. The configuration file is selected at the beginning of the computation via standard input. In this way, the user can compare the different results obtained in the four main cases of interest mentioned in the Configuration files section and explained in the Evaluation section.

#### Tests

The test files provided are:

- TestSimulationPolicy: a file to test the SimulationPolicy object, with tests for the following methods:
  
    - createCloudlet(): to check if the number of the created cloudlets in the container is correct and if the number of users is correctly associated to each cloudlet;
    - createVM(): to check if the number of the created VMs in the container is correct and if the number of users is correctly associated to each cloudlet;
    - createDatacenter(): to check if the Datacenter object is correctly instantiated and if the number of hosts is correct;
    - createMasterNode(): to check if the MasterNode object is correctly instantiated;
  
### Java

#### Extended classes

The following are the classes extended from the CloudSim framework:

- MasterNode: extends the DatacenterBroker class, providing further functionalities such as the simulation of the map/reduce task with dynamic allocation of reducers, and the scheduling policy based on CPU cores;
- NewCloudlet: extends the Cloudlet class, providing the distinction between different kinds of cloudlets (MAPPER, REDUCER and GENERAL)

### Configuration files

The eight configuration files allow to run eight distinct simulation just running the SimulationPolicy.scala program, leveraging at their best the advantages of having parameters in configuration files instead of hardcoding them in the code. They present in couples the same configuration, but respectively enabling or not the scheduling policy. For example, noPolicy1.conf is the configuration file for the first case without the policy, while withPolicy1.conf is the first case with the policy. The four main cases generated are explained in the Evaluation section. 

### Parameters

The parameters that vary among the configuration files are highlighted and do not show the correspondent value. 

- Main: contains the parameters used in the main method of the simulation.

      - num_user = 2, the number of users
      - trace_flag = false, boolean to trace events
      - bw_0 = 10.0, the bandwidth set for the first datacenter
      - bw_1 = 10.0, the bandwidth set for the second datacenter
      - lat_0 = 1, the latency set for the fist datacenter
      - lat_1 = 2, the latency set for the second datacenter
      - idShift = 0, base ID to increase for ID assignment
      - cloudlet_types = 2, the distinct types of cloudlets (mapper/reducer/general)
      - vm_types = 2, the distinct types of VMs (CPU, memory, etc.)
  
- Cloudlet_0: contains the parameters for the first type of cloudlet (mapper).

      - length = 20000, millions of instructions executed
      - fileSize = 300, input size
      - outputSize = 300, output size
      - pesNumber = 1, number of processing elements required
      - **count, the number of cloudlet to generate**
      - type = 0, integer representing the cloudlet type (mapper in this case)
  
- Cloudlet_1: contains the parameters for the second type of cloudlet (general).

      - length = 40000, millions of instructions executed
      - fileSize = 300, input size
      - outputSize = 300, output size
      - pesNumber = 2, number of processing elements required
      - **count, the number of cloudlet to generate**
      - type = 2, integer representing the cloudlet type (general in this case)
  
- Cloudlet_2: contains the parameters for the third type of cloudlet (reducer). Note that in this case the parameter "count" is not needed, since we are going to generate the cloudlets at runtime depending on the number of submitted mappers.
  
      - length = 20000, millions of instructions executed
      - fileSize = 600, input size (sum of the output of 2 mappers, since in the given configuration each reducer takes the results of 2 mappers)
      - outputSize = 600, output size
      - pesNumber = 1, number of processing elements required
      - type = 0, integer representing the cloudlet type (reducer in this case)

- vm_0: contains the parameters for the first type of VM.
  
      - idShift = 0, base ID to increase for ID assignment
      - size = 524288, storage size allocated for the VM
      - ram = 32768, RAM allocated for the VM
      - mips = 500, MIPS of the VM
      - bw = 1000, bandwidth of the VM
      - pesNumber = 2, number of PEs assigned to the VM
      - vmm = "Xen", VMM name
      - **count, number of VM to create**

- vm_1: contains the parameters for the second type of VM.

      - idShift = 100, base ID to increase for ID assignment
      - size = 524288, storage size allocated for the VM
      - ram = 32768, RAM allocated for the VM
      - mips = 500, MIPS of the VM
      - bw = 1000, bandwidth of the VM
      - pesNumber = 1, number of PEs assigned to the VM
      - vmm = "Xen", VMM name
      - **count, number of VM to create**

- Datacenter: contains the parameters for the datacenter.

      - mips_0 = 5000, MIPS of the first host
      - mips_1 = 5000, MIPS of the second host
      - host_types = 2, number of distinct host types
      - cpu_types = 2, number of distinct CPU types
      - hostCount_0 = 1, number of type 0 hosts to create
      - hostCount_1 = 1, number of type 1 hosts to create
      - hostId_0 = 0, first host ID
      - hostId_1 = 1, second host ID
      - cores_0 = 2, cores assigned of CPU type
      - cores_1 = 1, cores of the second CPU type
      - ram = 262144, RAM provided by the datacenter
      - storage = 2097152, storage provided by the datacenter
      - bw = 10000, bandwidth provided by the datacenter
      - arch = "x86", system architecture
      - os = "Linux", operating system
      - vmm = "Xen", VMM name
      - time_zone = 10.0, time zone where this resource is located
      - cost = 3.0, processing cost
      - costPerMem = 0.05, memory cost
      - costPerStorage = 0.1, storage cost
      - costPerBw = 0.1, bandwidth cost
      - peCostPerSec = 0.03, cost per second

- Masternode: contains the parameters needed in the MasterNode class implementation.

      - mapreduce = 2, the number of mappers associated to each reducer (e.g. in this case each reducer is dynamically allocated every 2 mappers)
      - **sched_policy = true if the scheduling policy is enabled, false otherwise**


## Implementation of the map/reduce task
 
 As mentioned above, the framework has been extended in order to enable the execution of map/reduce tasks. First off, an extension of the Cloudlet class (NewCloudlet) has been implemented, in order to distinguish between different types of cloudlets (mappers, reducers, and general; the last one represents any other task). Then, the DatacenterBroker class was extended to obtain the MasterNode class. The override of its processEvent(simEvent ev) method distinguishes between the different types of cloudlets and performs different actions in response to the CLOUDLET_RETURN event. If the returned cloudlet is of type GENERAL (i.e. any other cloudlet that is not a mapper), the CLOUDLET_RETURN event is processed as in the default implementation of the DatacenterBroker. If the cloudlet is a MAPPER instead, the MasterNode starts instantiates the counter of the returned mappers. Whenever the counter arrives to a certain value specified in the configuration file as "mapreduce" (2 in our case), the MasterNode generates a reduce task that takes the output of the completed mappers as input. In other words, a new reducer is generated as soon as n mappers have terminated. Once the reducer is submitted, the counter is cleared and instantiated again as soon as the next mapper returns a result. If the number of mappers left in the list is less than the number of mappers that provide the input to a reducer, a reducer will be generated to handle the remaining mappers (i.e. if the number of mappers % mapreduce value != 0).  
 
## Scheduling policy

### Description

The implemented scheduling policy takes into account the loss of performance introduced in multi-cores CPUs by the communication channels between cores and the information exchange between them. This loss introduces a slight delay in the computation that might increase the overall timing if cloudlets that need only a single processing unit are scheduled on VMs for which multiple cores were allocated. With the default scheduling policy, the VMs are added in a list and the cloudlets are scheduled just following the list order, without taking in consideration the processing elements needed by the cloudlets and provided by the VMs. The implemented policy instead schedules cloudlets as follows: if the cloudlet needs a lower number of processing units than the ones allocated in the first choice VM, the MasterNode will search for a new VM that provides possibly the same number of processing elements required by the cloudlet for its execution. In our particular case, we are considering single-core and dual-cores CPUs, and cloudlets that either need one or two cores. An index is used to keep track of the last single-core VM used to process a cloudlet that requires a single core. The MasterNode searches among the VMs, starting from the last single-core VM allocated. If there is no other single-core machine available, the cloudlet will be submitted to a dual-core machine. In this way, the MasterNode will try to use as many single-core VMs as possible to execute the cloudlets that require a single core, leaving as many dual-core VMs as possible for those cloudlets that require 2 cores. This feature is well shown in the results of the simulations, since in this configuration general cloudlets require 2 PEs while mappers require just one PE, and cloudlets of both types are submitted together at the same time. As we can see from the results, the MasterNode allocates on single-cores VMs as many single-PE cloudlets as possible. If the configuration provides enough single-cores VMs to run as many single-PE cloudlets as possible, this policy leads to a reduction of both execution time and overall processing cost. The single-core/dual-core case can be easily extended to cases with multiple multi-cores machines. 

### Evaluation

Four distinct cases can be defined to evaluate our policy, depending on the four possible combinations of the number of VMs and cloudlets in the configuration: 

1. \# of single-core VMs ≥ \# of dual-core VMs & \# of single-core cloudlets ≥ \# of dual-core cloudlets;
2. \# of single-core VMs ≥ \# of dual-core VMs & \# of single-core cloudlets < \# of dual-core cloudlets;
3. \# of single-core VMs < \# of dual-core VMs & \# of single-core cloudlets < \# of dual-core cloudlets;
4. \# of single-core VMs < \# of dual-core VMs & \# of single-core cloudlets ≥ \# of dual-core cloudlets;

In the first three cases, the policy is effective: if the master node takes into account the number of cores needed by the cloudlets and searches for a VM with an appropriate number of cores, both the execution time and the overall cost of the computation are reduced. In case 1, this happens because we have a higher number of single-core VMs, and submitting as many single-core cloudlets as possible to the available single-core VMs allows to reduce the overhead introduced by the communication delay of multi-core CPUs. In case 2 and 3 instead, the policy is effective because we have a larger number of dual-core cloudlets, and the policy allows not to waste dual-core VMs' CPUs with the computation of single-core cloudlets that can be submitted to single-core VMs.

The fourth case represents the limitation of the implemented policy. If we have both a larger number of dual-core VMs and dual-core cloudlets, trying to submit single-core cloudlets to single-core machines is just a waste of time, since we have enough dual-core machines to execute both single-core and multi-core tasks. The cost is still reduced, but by an amount that is not relevant compared to the increase of the execution time.

## Results


