**This text is written as a part of the article on completely random reactor model.**

## Macroscopic simulation
Contrary to the microscopic simulation implemented in GEANT4 engine, the macroscopic simulation does not take into account individual scatterings of electron on gas molecules and the development of electromagnetic avalanche. Instead, it considers only the transport of high energy photons between different parts of the thundercloud and the macroscopic acceleration cell distribution. The macroscopic is unavoidable when doing simulations on a large spatial and time scales because the complexity of microscopic model. One must note that the acceleration cell is not an isolated entity. One electron acceleration region could smoothly transform to another region without any obvious borders. The term acceleration cell is introduced only for convenience and better mental model.

The macroscopic model was done in [Kotlin language](https://aip.scitation.org/doi/abs/10.1063/1.5130103). The source code and distributions are avaolable in its [github repository](https://github.com/mipt-npm/rl-tge-sim).

Current (v1.0.0) simulation operates in terms of two types of particles:
* Runway electron (with energy above Gurevich critical energy for given altitude and electric field).
* A photon, with the energy above the energy of runway electron (so it could create a runway electron in the photo-ionization process).

Other particles (like positrons) are not considered right now, but could be added later. Each particle stores its energy. origin point velocity vector and energy.

The interaction of particles with the atmosphere and their propagation is described with the `Atmosphere` interface, which contains following properties and methods:

* `val field: Field` describes the electric field. The field in specific point is generated on-demand, so it could be defined by a formula or loaded from file if needed.
* `fun Photon.interactionPoint(): Vector3D` computes the interaction point for a high energy photon. The interaction is random and depends on the provided random generator.
* `fun Photon.convert(point: Vector3D): Electron` converts a photon to an electron via any provided physical mechanism ( (photo-effect, Compton effect, etc). The conversion is also random and could depend on external tables if needed.
* `fun Electron.accelerate(point: Vector3D = origin): Collection<Particle>` uses the results of microscopic simulation to produce the results of electron acceleration from a given point. The result of this function is a collection of particles (photons and electrons) together with their energies and origin points. The resulting collection could be empty, which means that no high energy particles are born from given electron.

Those methods when combined allow to compute all output particles from a single "cell ignition" by a photon. The resulting particles could be then propagated further by the model.

### Simple atmosphere
In this work we use the simplified atmosphere model, which is created only to show the general feasibility of completely random reactor model, not to show any specific numeric results. The primary factor - the real distribution of electric field is unknown, which makes precise computation of other parameters useless. The simplified `Atmosphere` includes following rules:
*  The interaction point of photon lies on the straight line from its origin point alongside its velocity vector and is distributed with exponential distribution with given mean free path. This assumption is close to realistic.
*  The photon always produces the electron with the same energy and direction, meaning that process is always the photo-effect. This is not quite correct from the physical point since at these energies Compton effect usually dominates, but the distinction does not strongly affect the general model.
* The field in the cell has a randomly distributed direction and does not have any fixed field map.
* The acceleration cell produces several photons offset by the average acceleration cell size in the direction of the cell field. All photons have the same direction (alongside the cell field) and energy. The number of photons follows the Poisson distribution with given average which is called local multiplication factor.

The simplified model has important advantage: it has only two important parameters: the size of the cloud (it is considered cubic) and the local multiplication factor. Another parameter - the offset between the ignition point and photons origin point has only small effect on the simulation result. The local multiplication factor includes all known and unknown factors of the model including the angle between the electron velocity and the cell electric field (for large angles, the electron "dies" without starting the avalanche) and actual distribution of the field inside the cell. GEANT4 simulation of individual acceleration cells show that this factor could be quite different for different parameters, but the values like 2.0 could be easily achieved for acceleration cells of 300 m.

### Generational computation model
The simulation does not directly track the time, instead each particle has a generation number attached to it. The number of generation for each subsequent particle is increased by one relative to the parent particle generation number. The timescale for all processes is more or less the same since all particles are relativistic. One generation is about 300 meters with the speed of light, which equals to $1~\mu s$. All particles in one generation are computed in parallel with automatic scaling on the number of processor cores present in the system. The computation of the generation is done lazily, which means that the next generation is computed only when it is requested. It allows to automatically stop the simulation when the number of particles in the simulation exceeds the given threshold (it is quite useful for exponential processes)

### Simulation results
**TBD**
