package com.gfisca2;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.compatibility.hazelcast.HazelSim;

import java.util.ArrayList;

/**
 * NewCloudlet is an extension of the Cloudlet class.
 * It allows the distinction between 3 different types of cloudlets:
 * - Mappers
 * - Reducers
 * - General (any other task)
 *
 * @author Giorgia Fiscaletti
 */
public class NewCloudlet extends Cloudlet {


    private HazelSim objectCollection = HazelSim.getHazelSim();

    /**
     * The Vm where the cloudlet is running.
     */
    private Vm associatedVm;

    /**
     * Enum to distinguish the 3 different types of cloudlets.
     */
    public enum Type {
        MAPPER, REDUCER, GENERAL
    }

    /**
     * ArrayList of indexes of the eventual mappers associated to the reducer,
     * if the cloudlet is a reducer.
     */
    private ArrayList<Integer> associatedMappers;

    /**
     * The type of the cloudlet.
     */
    private Type cloudletType;

    /**
     * Allocates a new NewCloudlet object. The NewCloudlet length, input and output file sizes should be
     * greater than or equal to 1. By default this constructor sets the history of this object.
     *
     * @param cloudletId          the unique ID of this NewCloudlet
     * @param cloudletLength      the length or size (in MI) of this cloudlet to be executed in a
     *                            PowerDatacenter
     * @param cloudletFileSize    the file size (in byte) of this cloudlet <tt>BEFORE</tt> submitting
     *                            to a PowerDatacenter
     * @param cloudletOutputSize  the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
     *                            executing by a PowerDatacenter
     * @param pesNumber           the pes number
     * @param utilizationModelCpu the utilization model cpu
     * @param utilizationModelRam the utilization model ram
     * @param utilizationModelBw  the utilization model bw
     * @param type                the cloudlet type, (0 - MAPPER, 1 - REDUCER, other - GENERAL)
     * @pre cloudletID >= 0
     * @pre cloudletLength >= 0.0
     * @pre cloudletFileSize >= 1
     * @pre cloudletOutputSize >= 1
     * @post $none
     */
    public NewCloudlet(final int cloudletId,
                       final long cloudletLength,
                       final int pesNumber,
                       final long cloudletFileSize,
                       final long cloudletOutputSize,
                       final UtilizationModel utilizationModelCpu,
                       final UtilizationModel utilizationModelRam,
                       final UtilizationModel utilizationModelBw,
                       final int type) {

        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw);
        setCloudletType(type);
        associatedMappers = new ArrayList<>();
        associatedVm = null;
    }

    /**
     * Gets the type of this NewCloudlet.
     *
     * @return Cloudlet Type
     * @pre $none
     * @post $none
     */
    public Type getCloudletType() { return cloudletType; }

    /**
     * Sets the NewCloudlet type.
     *
     * @param n an index for the cloudlet type (0 - MAPPER, 1 - REDUCER, other - GENERAL)
     * @pre n >= 0
     * @post $none
     */
    private void setCloudletType(int n) {

        switch(n) {
            case 0:
                cloudletType = Type.MAPPER;
                break;
            case 1:
                cloudletType = Type.REDUCER;
                break;
            default:
                cloudletType = Type.GENERAL;
                break;
        }

    }

    /**
     * Sets the ArrayList of indexes of the associated mappers, if the cloudlet is a reducer.
     *
     * @param associatedMappers the ArrayList of indexes
     * @pre type = REDUCER
     * @post $none
     */
    void setAssociatedMappers(ArrayList<Integer> associatedMappers) {
        this.associatedMappers.addAll(associatedMappers);
    }

    /**
     * Overrides the method to get the finish time of this NewCloudlet in a CloudResource.
     * Adds the eventual overhead of the multi-core CPU.
     *
     * @return the finish or completion time of this Cloudlet or <tt>-1</tt> if not finished yet.
     * @pre $none
     * @post $result >= -1
     */
    @Override
    public double getFinishTime() {
        double multiCoreOverhead = 0;
        //Loss of performance generated in multi-cores processors by the communication channels between the cores.
        //The values are for demonstration purposes only.
        if(associatedVm.getNumberOfPes() > 1)
            multiCoreOverhead = 0.7 * associatedVm.getNumberOfPes();
        if (isHz) {
            double finishTime = 0.0;
            if (objectCollection.getCloudletFinishedTime().get(getCloudletId()) != null) {
                finishTime = objectCollection.getCloudletFinishedTime().get(getCloudletId());
            }
            return finishTime;
        }
        return finishTime + multiCoreOverhead;
    }

    /**
     * Gets the ArrayList of indexes of the associated mappers, if the cloudlet is a reducer.
     *
     * @return List of indexes of the associated mappers
     * @pre type = REDUCER
     * @post $none
     */
    public ArrayList<Integer> getAssociatedMappers() { return associatedMappers; }

    void setAssociatedVm(Vm associatedVm) {
        this.associatedVm = associatedVm;
    }

    /**
     * Returns a more readable ID for the final table.
     *
     * @return an ID composed by a letter representing the cloudlet type + the cloudlet id
     * @pre type = REDUCER
     * @post $none
     */
    public String getCloudletFullId() {
        switch (cloudletType) {
            case MAPPER:
                return "M_" + getCloudletId();
            case REDUCER:
                return "R_" + getCloudletId();
            default:
                return "G_" + getCloudletId();
        }
    }

}
