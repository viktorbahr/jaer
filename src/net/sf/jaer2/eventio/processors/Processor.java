package net.sf.jaer2.eventio.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.sf.jaer2.chips.Chip;
import net.sf.jaer2.eventio.ProcessorChain;
import net.sf.jaer2.eventio.eventpackets.EventPacketContainer;
import net.sf.jaer2.eventio.events.Event;
import net.sf.jaer2.util.GUISupport;
import net.sf.jaer2.util.Reflections;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Processor implements Runnable {
	/**
	 * Enumeration containing the available processor types and their string
	 * representations for printing.
	 */
	public static enum ProcessorTypes {
		INPUT_PROCESSOR("Input"),
		OUTPUT_PROCESSOR("Output"),
		EVENT_PROCESSOR("Event");

		private final String str;

		private ProcessorTypes(final String s) {
			str = s;
		}

		@Override
		public final String toString() {
			return str;
		}
	}

	/** Local logger for log messages. */
	protected final static Logger logger = LoggerFactory.getLogger(Processor.class);

	/** Processor identification ID. */
	protected final int processorId;
	/** Processor identification Name. */
	protected final String processorName;

	/** Chain this processor belongs to. */
	protected final ProcessorChain parentChain;
	/** Previous processor in the ordered chain. */
	private Processor prevProcessor;
	/** Next processor in the ordered chain. */
	private Processor nextProcessor;

	// Processor type management
	/** Defines which Event types this Processor can work on. */
	private final ObservableSet<Class<? extends Event>> compatibleInputTypes = FXCollections
		.observableSet(new HashSet<Class<? extends Event>>(4));
	/** Defines which Event types this Processor creates and then outputs. */
	private final ObservableSet<Class<? extends Event>> additionalOutputTypes = FXCollections
		.observableSet(new HashSet<Class<? extends Event>>(4));

	// Processor stream management
	/** Defines which streams of events this Processor can work on. */
	private final ObservableList<ImmutablePair<Class<? extends Event>, Integer>> inputStreams = FXCollections
		.observableArrayList();
	/**
	 * Defines which streams of events this Processor will work on, based on
	 * user configuration.
	 */
	private ObservableList<ImmutablePair<Class<? extends Event>, Integer>> selectedInputStreams = null;
	/**
	 * Defines which streams of events this Processor can output, based upon the
	 * Processor itself and all previous processors before it in the chain.
	 */
	private final ObservableList<ImmutablePair<Class<? extends Event>, Integer>> outputStreams = FXCollections
		.observableArrayList();

	/** Queue containing all containers to process. */
	protected final BlockingQueue<EventPacketContainer> workQueue = new ArrayBlockingQueue<>(16);
	/**
	 * List containing all containers that are currently being worked on (inside
	 * the Processor, not thread-safe!).
	 */
	protected final List<EventPacketContainer> toProcess = new ArrayList<>(32);

	/** Main GUI layout - Horizontal Box. */
	protected final HBox rootLayout = new HBox(10);

	/** Configuration GUI layout - Vertical Box. */
	protected final VBox rootConfigLayout = new VBox(10);
	/** Configuration GUI: tasks to execute on success. */
	protected final List<Runnable> rootConfigTasks = new ArrayList<>(2);

	public Processor(final ProcessorChain chain) {
		parentChain = chain;

		processorId = parentChain.getNextAvailableProcessorID();
		processorName = getClass().getSimpleName();

		// Fill in the type information from the extending sub-classes.
		setCompatibleInputTypes(compatibleInputTypes);
		setAdditionalOutputTypes(additionalOutputTypes);

		// Inflate compatibleInputTypes, so as to also consider sub-classes.
		final Set<Class<? extends Event>> inflatedCompatibleInputTypes = new HashSet<>();
		for (final Class<? extends Event> clazz : compatibleInputTypes) {
			inflatedCompatibleInputTypes.addAll(Reflections.getSubClasses(clazz));
		}
		compatibleInputTypes.addAll(inflatedCompatibleInputTypes);

		// Build GUIs for this processor.
		buildConfigGUI();
		buildGUI();

		Processor.logger.debug("Created Processor {}.", this);
	}

	/**
	 * Return the ID number of this processor.
	 *
	 * @return processor ID number.
	 */
	public final int getProcessorId() {
		return processorId;
	}

	/**
	 * Return the name of this processor.
	 *
	 * @return processor name.
	 */
	public final String getProcessorName() {
		return processorName;
	}

	/**
	 * Return the chain this processor belongs to.
	 *
	 * @return parent chain.
	 */
	public final ProcessorChain getParentChain() {
		return parentChain;
	}

	public final Processor getPrevProcessor() {
		return prevProcessor;
	}

	public final void setPrevProcessor(final Processor prev) {
		prevProcessor = prev;

		// StreamSets depend on the previous processor.
		rebuildStreamSets();
	}

	public final Processor getNextProcessor() {
		return nextProcessor;
	}

	public final void setNextProcessor(final Processor next) {
		nextProcessor = next;
	}

	protected abstract void setCompatibleInputTypes(Set<Class<? extends Event>> inputs);

	protected abstract void setAdditionalOutputTypes(Set<Class<? extends Event>> outputs);

	protected final void regenerateAdditionalOutputTypes(final Collection<Class<? extends Event>> newOutputs) {
		additionalOutputTypes.clear();

		additionalOutputTypes.addAll(newOutputs);

		rebuildStreamSets();
	}

	private final class StreamComparator implements Comparator<ImmutablePair<Class<? extends Event>, Integer>> {
		@Override
		public int compare(final ImmutablePair<Class<? extends Event>, Integer> stream1,
			final ImmutablePair<Class<? extends Event>, Integer> stream2) {
			if (stream1.right > stream2.right) {
				return 1;
			}

			if (stream1.right < stream2.right) {
				return -1;
			}

			return 0;
		}
	}

	private void rebuildInputStreams() {
		if (prevProcessor != null) {
			final List<ImmutablePair<Class<? extends Event>, Integer>> compatibleInputStreams = new ArrayList<>();

			// Add all outputs from previous Processor, filtering incompatible
			// Event types out.
			for (final ImmutablePair<Class<? extends Event>, Integer> stream : prevProcessor.getAllOutputStreams()) {
				if (compatibleInputTypes.contains(stream.left)) {
					compatibleInputStreams.add(stream);
				}
			}

			// Sort intermediate list.
			Collections.sort(compatibleInputStreams, new StreamComparator());

			// Replace with new data in a non-destructive way, by not touching
			// values that were already present.
			for (final Iterator<ImmutablePair<Class<? extends Event>, Integer>> iter = inputStreams.iterator(); iter
				.hasNext();) {
				final ImmutablePair<Class<? extends Event>, Integer> element = iter.next();

				if (compatibleInputStreams.contains(element)) {
					compatibleInputStreams.remove(element);
				}
				else {
					iter.remove();
				}
			}

			// Add remaining values that weren't yet present.
			inputStreams.addAll(compatibleInputStreams);
		}
		else {
			inputStreams.clear();
		}
	}

	public final ObservableList<ImmutablePair<Class<? extends Event>, Integer>> getAllInputStreams() {
		return FXCollections.unmodifiableObservableList(inputStreams);
	}

	private void rebuildOutputStreams() {
		final List<ImmutablePair<Class<? extends Event>, Integer>> allOutputStreams = new ArrayList<>();

		// Add all outputs from previous Processor, as well as outputs produced
		// by the current Processor.
		if (prevProcessor != null) {
			allOutputStreams.addAll(prevProcessor.getAllOutputStreams());
		}

		for (final Class<? extends Event> outputType : additionalOutputTypes) {
			allOutputStreams.add(new ImmutablePair<Class<? extends Event>, Integer>(outputType, processorId));
		}

		// Sort intermediate list.
		Collections.sort(allOutputStreams, new StreamComparator());

		outputStreams.clear();
		outputStreams.addAll(allOutputStreams);
	}

	public final ObservableList<ImmutablePair<Class<? extends Event>, Integer>> getAllOutputStreams() {
		return FXCollections.unmodifiableObservableList(outputStreams);
	}

	private void rebuildStreamSetsInternal() {
		Processor.logger.debug("Rebuilding StreamSets for {}.", toString());

		rebuildInputStreams();
		rebuildOutputStreams();

		// Call recursively on the next Processor, so that the rest of the chain
		// gets updated correctly.
		if (nextProcessor != null) {
			nextProcessor.rebuildStreamSetsInternal();
		}
	}

	public final void rebuildStreamSets() {
		// Ensure previous is updated, to show last box in GUI.
		if (prevProcessor != null) {
			prevProcessor.rebuildStreamSetsInternal();
		}
		else {
			// If previous is not defined, let's start from here (this).
			rebuildStreamSetsInternal();
		}
	}

	public final ObservableList<ImmutablePair<Class<? extends Event>, Integer>> getAllSelectedInputStreams() {
		// This is strictly a subset of inputStreams and is automatically
		// updated when either inputStreams or the selection is changed, thanks
		// to JavaFX observables and bindings.
		return FXCollections.unmodifiableObservableList(selectedInputStreams);
	}

	public final Processor getProcessorForSourceId(final int sourceId) {
		// Search backwards for given source ID and get the corresponding
		// Processor. A source *must* be either the current Processor or
		// upstream of it.
		if (sourceId == processorId) {
			return this;
		}

		if (prevProcessor == null) {
			return null;
		}

		return prevProcessor.getProcessorForSourceId(sourceId);
	}

	public final Chip getChipForSourceId(final int sourceId) {
		final Processor procSource = getProcessorForSourceId(sourceId);

		if ((procSource != null) && (procSource instanceof InputProcessor)) {
			return ((InputProcessor) procSource).getInterpreterChip();
		}

		return null;
	}

	/**
	 * Check if a container is to be processed by this processor.
	 * This is the case if it contains <Type, Source> combinations that are
	 * relevant, based upon the configuration done by the user.
	 *
	 * @param container
	 *            the EventPacket container to check.
	 * @return whether relevant EventPackets are present or not.
	 */
	public final boolean processContainer(final EventPacketContainer container) {
		for (final ImmutablePair<Class<? extends Event>, Integer> relevant : selectedInputStreams) {
			if (container.getPacket(relevant.left, relevant.right) != null) {
				return true;
			}
		}

		return false;
	}

	public final void add(final EventPacketContainer container) {
		workQueue.add(container);
	}

	public final void addAll(final Collection<EventPacketContainer> containers) {
		workQueue.addAll(containers);
	}

	/**
	 * Get the graphical layout corresponding to this class, so that it can be
	 * displayed somewhere by adding it to a Scene.
	 *
	 * @return GUI reference to display.
	 */
	public final Pane getGUI() {
		return rootLayout;
	}

	/**
	 * Create the base GUI elements and add them to the rootLayout.
	 */
	private void buildGUI() {
		// Create box holding information and controls for the Processor.
		final VBox controlInfoBox = new VBox(5);
		controlInfoBox.setPadding(new Insets(5));
		controlInfoBox.setStyle("-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: black");
		rootLayout.getChildren().add(controlInfoBox);

		GUISupport.addLabel(controlInfoBox, toString(), null, null, null);

		// Create a box holding the configuration buttons.
		final HBox buttonBox = new HBox(5);
		controlInfoBox.getChildren().add(buttonBox);

		GUISupport.addButtonWithMouseClickedHandler(buttonBox, "Remove Processor", false, "/icons/Remove.png",
			new EventHandler<MouseEvent>() {
				@Override
				public void handle(@SuppressWarnings("unused") final MouseEvent event) {
					parentChain.removeProcessor(Processor.this);
				}
			});

		GUISupport.addButtonWithMouseClickedHandler(buttonBox, "Configure Processor", false, "/icons/Gear.png",
			new EventHandler<MouseEvent>() {
				@Override
				public void handle(@SuppressWarnings("unused") final MouseEvent event) {
					GUISupport.showDialog("Processor Configuration", rootConfigLayout, rootConfigTasks);
				}
			});

		// Create a box holding information about the <type, source>
		// combinations that are currently being processed.
		final VBox selectedInputStreamsBox = new VBox(5);
		controlInfoBox.getChildren().add(selectedInputStreamsBox);

		selectedInputStreams.addListener(new ListChangeListener<ImmutablePair<Class<? extends Event>, Integer>>() {
			@Override
			public void onChanged(final Change<? extends ImmutablePair<Class<? extends Event>, Integer>> change) {
				selectedInputStreamsBox.getChildren().clear();

				if (!change.getList().isEmpty()) {
					GUISupport.addLabel(selectedInputStreamsBox, "Currently processing:", null, null, null);

					for (final ImmutablePair<Class<? extends Event>, Integer> selInStream : change.getList()) {
						GUISupport.addLabel(selectedInputStreamsBox, String.format("Type %s from Source %d",
							selInStream.left.getSimpleName(), selInStream.right), null, null, null);
					}
				}
			}
		});

		// Create a box holding information about the types in transit.
		final VBox typesBox = new VBox(5);
		rootLayout.getChildren().add(typesBox);

		outputStreams.addListener(new ListChangeListener<ImmutablePair<Class<? extends Event>, Integer>>() {
			@Override
			public void onChanged(final Change<? extends ImmutablePair<Class<? extends Event>, Integer>> change) {
				typesBox.getChildren().clear();

				// Only add elements to show outputs if we're not the last box.
				if ((nextProcessor != null) && !change.getList().isEmpty()) {
					GUISupport.addArrow(typesBox, 150, 2, 10, 6);

					for (final ImmutablePair<Class<? extends Event>, Integer> outStream : change.getList()) {
						GUISupport.addLabel(typesBox,
							String.format("Type %s from Source %d", outStream.left.getSimpleName(), outStream.right),
							null, null, null);
					}
				}
			}
		});
	}

	/**
	 * Get the graphical layout for the configuration screen corresponding to
	 * this class, so that it can be
	 * displayed somewhere by adding it to a Scene.
	 *
	 * @return GUI reference to display.
	 */
	public final Pane getConfigGUI() {
		return rootConfigLayout;
	}

	/**
	 * Create the base GUI elements for the configuration screen and add them to
	 * the rootConfigLayout.
	 */
	private void buildConfigGUI() {
		final ListView<ImmutablePair<Class<? extends Event>, Integer>> streams = new ListView<>();

		// Set content to auto-updating ObservableList, enable multiple
		// selections and fix the height to something sensible.
		streams.setItems(inputStreams);
		streams.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		streams.setPrefHeight(140);

		// Make the selected items globally available.
		selectedInputStreams = streams.getSelectionModel().getSelectedItems();

		GUISupport.addLabelWithControlsVertical(rootConfigLayout, "Select streams to process:",
			"Select the <Type, Source> combinations (streams) on which to operate.", streams);
	}

	@Override
	public String toString() {
		return String.format("%s - ID %d", processorName, processorId);
	}
}
