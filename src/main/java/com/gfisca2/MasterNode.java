package com.gfisca2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.ArrayList;
import java.util.List;

/**
 * MasterNode is an extension of the Datacenter Broker. It hides VM management, as vm creation, sumbission of cloudlets
 * to this VMs and destruction of VMs. Moreover, it handles the dynamic creation of reducing task associated to
 * the eventually submitted mappers.
 *
 * @author Giorgia Fiscaletti
 */
public class MasterNode extends DatacenterBroker {

    /**
     * List of reducers.
     */
    private List<NewCloudlet> reducerList;
    /**
     * ArrayList of indexes of the eventual mappers associated to the reducer,
     * if the cloudlet is a reducer.
     */
    private ArrayList<Integer> associatedMappers; //array of indexes of the mappers associated to each reducer
    /**
     * Primary index needed for the selection of the VM on which to execute the cloudlet.
     */
    private int vmIndex = 0;
    /**
     * Secondary index needed for the selection of the VM on which to execute the cloudlet, useful
     * for the scheduling policy defined below.
     */
    private int vmSecondIndex = -1;

    /**
     * Index that keeps trace of the mappers that have completed their execution to generate the reducing tasks.
     */
    private int doneMapper = 0;
    /**
     * Index that keeps trace of the generated reducers.
     */
    private int reducers = 0;
    /**
     * Initialization of the parameter to load the configuration.
     */
    private Config conf = ConfigFactory.load();

    /**
     * Created a new MasterNode object.
     *
     * @param name name to be associated with this entity (as required by Sim_entity class from
     *            simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public MasterNode(String name) throws Exception {
        super(name);
        setReducerList(new ArrayList<>());
        setCloudletList(new ArrayList<>());
        associatedMappers = new ArrayList<>();
    }

    /**
     * Ovverrides the method that processes events available for the MasterNode, to allow the dynamic generation of reducers.
     * The reducers are generated dynamically by the master node. The master node associates a reducer to
     * x mappers, x being defined in the configuration file. More precisely, as soon as the first x mappers
     * complete their execution, the master node will generate a reduce task that will start right away
     * (after the delay defined in the network links, that varies if the reducer is instantiated in another
     * datacenter. With this approach, there is no need to assume a priori the number of reducers
     * needed to carry out the given task, and even if not all the mappers have finished, the newly
     * generated reducers can start their computation.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Resource characteristics request
            case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
                processResourceCharacteristicsRequest(ev);
                break;
            // Resource characteristics answer
            case CloudSimTags.RESOURCE_CHARACTERISTICS:
                processResourceCharacteristics(ev);
                break;
            // VM Creation answer
            case CloudSimTags.VM_CREATE_ACK:
                processVmCreate(ev);
                break;
            // A finished cloudlet returned
            case CloudSimTags.CLOUDLET_RETURN:

                if(ev.getData().getClass() == NewCloudlet.class) {

                    NewCloudlet cloudlet = (NewCloudlet) ev.getData();

                    if (cloudlet.getCloudletType() == NewCloudlet.Type.MAPPER) {

                        associatedMappers.add(cloudlet.getCloudletId());
                        doneMapper++;
                        //dynamically generate a reducing task every x done mapping tasks, x defined in the configuration file
                        if (doneMapper % conf.getInt("masternode.mapreduce") == 0 || conf.getInt("cloudlet_0.count") == doneMapper) {
                            int reducersId = 0;
                            NewCloudlet reducer = new NewCloudlet(
                                    reducersId + reducers,
                                    conf.getInt("cloudlet_2.length"),
                                    conf.getInt("cloudlet_2.pesNumber"),
                                    conf.getInt("cloudlet_2.fileSize"),
                                    conf.getInt("cloudlet_2.outputSize"),
                                    new UtilizationModelFull(),
                                    new UtilizationModelFull(),
                                    new UtilizationModelFull(),
                                    1
                            );

                            reducer.setAssociatedMappers(associatedMappers);
                            reducer.setUserId(getId());
                            reducerList.add(reducer);
                            submitCloudletList(reducerList);
                            submitCloudlets();
                            reducerList.clear();
                            associatedMappers.clear();
                            reducers++;

                        }
                    }
                }
                processCloudletReturn(ev);
                break;
            // if the simulation finishes
            case CloudSimTags.END_OF_SIMULATION:
                shutdownEntity();
                break;
            // other unknown tags are processed by this method
            default:
                processOtherEvent(ev);
                break;
        }
    }

    /**
     * Overrides the method to submit cloudlets to the created VMs, adding a scheduling policy.
     * The scheduling policy based on CPU cores: the cloudlets that require less processing units will be assigned
     * to VMs with an appropriate number of PEs - i.e. the exact number of PEs available - if present and available
     * (assuming that the space sharing policy is applie for VM allocation). In this way, the VMs with more PEs
     * can be reserved for cloudlets that need more PEs. Moreover, since a overhead caused by the communication channels
     * between the cores in multi-cores CPUs has been implemented, this approach allows to minimize the impact
     * of the multi-cores CPUs overhead, avoiding their usage when not strictly necessary.
     *
     * @pre $none
     * @post $none
     */
    @Override
    protected void submitCloudlets() {
        List<NewCloudlet> successfullySubmitted = new ArrayList<>();
        if(vmSecondIndex == -1)
            vmSecondIndex = (vmIndex + 1) % getVmsCreatedList().size();
        for (NewCloudlet cloudlet : getCloudletList()) {
            int i = 0;
            Vm vm;
            // if user didn't bind this cloudlet and it has not been executed yet
            if (cloudlet.getVmId() == -1) {
                vm = getVmsCreatedList().get(vmIndex);
                // scheduling policy defined above
                if(conf.getBoolean("masternode.sched_policy"))
                    while(vm.getNumberOfPes() > cloudlet.getNumberOfPes() && i < getVmsCreatedList().size()) {
                        vm = getVmsCreatedList().get(vmSecondIndex);
                        vmSecondIndex = (vmSecondIndex + 1) % getVmsCreatedList().size();
                        i++;
                        if(i == getVmsCreatedList().size()) {
                            vm = getVmsCreatedList().get(vmIndex);
                         }
                    }
            } else { // submit to the specific vm
                vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
                if (vm == null) { // vm was not created
                    if(!Log.isDisabled()) {
                        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
                                cloudlet.getCloudletId(), ": bount VM not available");
                    }
                    continue;
                }
            }

            if (!Log.isDisabled()) {
                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
                        cloudlet.getCloudletId(), " to VM #", vm.getId());
            }

            cloudlet.setVmId(vm.getId());
            cloudlet.setAssociatedVm(vm);
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            cloudletsSubmitted++;
            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
            getCloudletSubmittedList().add(cloudlet);
            successfullySubmitted.add(cloudlet);
        }

        // remove submitted cloudlets from waiting list
        getCloudletList().removeAll(successfullySubmitted);
    }

    /**
     * Sets the List of generated reducers.
     *
     * @param reducerList the List of reducers
     * @pre $none
     * @post $none
     */
    private void setReducerList(List<NewCloudlet> reducerList) { this.reducerList = reducerList; }

    /**
     * Override of the cloudlet list getter, to return a list of NewCloudlets.
     *
     * @return the cloudlet list casted to NewCloudlet
     */
    @Override
    public List<NewCloudlet> getCloudletList() {
        return (List<NewCloudlet>) cloudletList;
    }

}
