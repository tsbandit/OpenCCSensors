package openccsensors.common.util;

import ic2.api.reactor.IC2Reactor;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.tile.IEnergyStorage;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class Ic2Utils {

	public static final int MASSFAB_MAX_ENERGY = 1100000;
	protected static final String MASS_FAB_CLASS = "ic2.core.block.machine.tileentity.TileEntityMatter";
	
	public static boolean isValidPowerTarget(Object target) {
		return target != null &&
			    (
			     target instanceof IEnergySink ||
			     target instanceof IEnergySource ||
			     target instanceof IEnergyConductor ||
			     target instanceof IEnergyStorage
			    );
	}

	public static boolean isValidMachineTarget(Object target) {
		return target != null &&
				 (
					target.getClass().getName() == MASS_FAB_CLASS ||
					target instanceof IReactor ||
					target instanceof IReactorChamber
				 );
	}
	
	public static HashMap getMachineDetails(World world, Object obj, boolean additional) {

		HashMap response = new HashMap();
		
		if (obj == null || !(obj instanceof TileEntity) || !additional) {
			return response;
		}

		TileEntity tile = (TileEntity) obj;
		
		IReactor reactor = null;
		if (tile instanceof IReactor) {
			reactor = (IReactor) tile;
		}else if (tile instanceof IReactorChamber) {
			reactor = ((IReactorChamber) tile).getReactor();
		}
		
		if (reactor != null) {
			int maxHeat = reactor.getMaxHeat();
			int heat = reactor.getHeat();
			response.put("Heat", heat);
			response.put("MaxHeat", maxHeat);
			response.put("Output", reactor.getOutput() * IC2Reactor.getEUOutput());
			response.put("Active", reactor.produceEnergy());
			response.put("HeatPercentage", 0);
			if (maxHeat > 0) {
				double heatPercentage = ((100.0 / maxHeat) * heat);
				response.put("HeatPercentage", Math.max(Math.min(heatPercentage, 100), 0));
			}
		}

		if (tile.getClass().getName() == MASS_FAB_CLASS) {
			NBTTagCompound tagCompound = getTagCompound(tile);
			response.put("Energy", tagCompound.getInteger("energy"));
			response.put("MaxEnergy", MASSFAB_MAX_ENERGY);
			response.put("Progress", 0);
			double progress = ((100.0 / MASSFAB_MAX_ENERGY) * tagCompound.getInteger("energy"));
			response.put("Progress",  Math.min(Math.max(0, progress), 100));
		}
		
		return response;
	}
	
	public static HashMap getPowerDetails(World world, Object obj, boolean additional) {
		
		HashMap response = new HashMap();
		
		if (obj == null || !(obj instanceof TileEntity) || !additional) {
			return response;
		}
		
		TileEntity tile = (TileEntity) obj;
		
		if (tile instanceof IEnergyStorage) {
			
			IEnergyStorage storage = (IEnergyStorage) tile;
			int capacity = storage.getCapacity();
			int stored = storage.getStored();
	
			response.put("Stored", stored);
			response.put("Capacity", capacity);
			response.put("Output", storage.getOutput());
			response.put("StoredPercentage", 0);

			if (capacity > 0) {
				response.put("StoredPercentage", Math.max(Math.min(100,((100.0 / capacity) * stored)), 0));
			}
		}
		
		if (tile instanceof IEnergySink ||
			tile instanceof IEnergySource ||
			tile instanceof IEnergyConductor) {
			
			long emitted = EnergyNet.getForWorld(world).getTotalEnergyEmitted(tile);
			long sunken = EnergyNet.getForWorld(world).getTotalEnergySunken(tile);
			response.put("EnergyEmitted", emitted);
			response.put("EnergySunken", sunken);
		
		}
		
		return response;
	}

	protected static NBTTagCompound getTagCompound(TileEntity tile) {
		NBTTagCompound tagCompound = new NBTTagCompound();
		tile.writeToNBT(tagCompound);
		return tagCompound;
	}
}
