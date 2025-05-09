// Immediate console log to verify script loading
console.log('Gallery.js is loading...');

// Global variables
let currentImageIndex = 0;
let galleryImages = [];
let imageModal;
let isInitialized = false;

// Initialize the modal when the script loads
function initializeImageModal() {
    try {
        console.log('Initializing image modal...');
        imageModal = document.createElement('div');
        imageModal.id = 'imageModal';
        imageModal.className = 'fixed inset-0 bg-black bg-opacity-90 z-50 hidden items-center justify-center';
        imageModal.innerHTML = `
            <div class="relative w-full h-full flex items-center justify-center">
                <button id="closeImageModal" class="absolute top-4 right-4 text-white text-4xl hover:text-gray-300">&times;</button>
                <button id="prevImage" class="absolute left-4 text-white text-4xl hover:text-gray-300">&lt;</button>
                <button id="nextImage" class="absolute right-4 text-white text-4xl hover:text-gray-300">&gt;</button>
                <img id="modalImage" class="max-h-[90vh] max-w-[90vw] object-contain" src="" alt="Full size image">
            </div>
        `;
        document.body.appendChild(imageModal);
        console.log('Image modal initialized successfully');
    } catch (error) {
        console.error('Error initializing image modal:', error);
    }
}

// Global modal functions
function showModal(modal) {
    try {
        console.log('Showing modal...');
        if (!modal) {
            console.error('Modal element is null or undefined');
            return;
        }
        modal.classList.remove('hidden');
        modal.classList.add('flex');
        console.log('Modal shown successfully');
    } catch (error) {
        console.error('Error showing modal:', error);
    }
}

function hideModal(modal) {
    try {
        console.log('Hiding modal...');
        if (!modal) {
            console.error('Modal element is null or undefined');
            return;
        }
        modal.classList.remove('flex');
        modal.classList.add('hidden');
        console.log('Modal hidden successfully');
    } catch (error) {
        console.error('Error hiding modal:', error);
    }
}

// Image Gallery functions
function showImageModal(index) {
    try {
        console.log('Showing image modal for index:', index);
        const modalImg = document.getElementById('modalImage');
        if (!modalImg) {
            console.error('Modal image element not found!');
            return;
        }
        if (!galleryImages[index]) {
            console.error('Gallery image not found for index:', index);
            return;
        }
        modalImg.src = galleryImages[index].src;
        currentImageIndex = index;
        showModal(imageModal);
        console.log('Image modal shown successfully');
    } catch (error) {
        console.error('Error showing image modal:', error);
    }
}

function showNextImage() {
    try {
        console.log('Showing next image...');
        currentImageIndex = (currentImageIndex + 1) % galleryImages.length;
        showImageModal(currentImageIndex);
    } catch (error) {
        console.error('Error showing next image:', error);
    }
}

function showPrevImage() {
    try {
        console.log('Showing previous image...');
        currentImageIndex = (currentImageIndex - 1 + galleryImages.length) % galleryImages.length;
        showImageModal(currentImageIndex);
    } catch (error) {
        console.error('Error showing previous image:', error);
    }
}

function initializeGallery() {
    if (isInitialized) {
        console.log('Gallery already initialized, skipping...');
        return;
    }

    try {
        console.log('Initializing gallery...');
        const galleryContainer = document.querySelector('.flex.overflow-x-auto');
        if (!galleryContainer) {
            console.error('Gallery container not found!');
            return;
        }
        
        // Get all images with the gallery-image class
        const images = document.querySelectorAll('.gallery-image');
        console.log('Found', images.length, 'gallery images');
        
        if (images.length === 0) {
            console.error('No gallery images found!');
            return;
        }
        
        // Store the images in our global array
        galleryImages = Array.from(images);
        
        // Add click handlers to all images
        images.forEach((img, index) => {
            console.log(`Adding click handler to image ${index}:`, img.src);
            img.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                console.log('Image clicked:', this.src);
                const index = parseInt(this.getAttribute('data-index'));
                console.log('Image index:', index);
                showImageModal(index);
            });
        });
        
        isInitialized = true;
        console.log('Gallery initialization complete');
    } catch (error) {
        console.error('Error initializing gallery:', error);
    }
}

// Initialize everything when the DOM is loaded
window.addEventListener('load', function() {
    console.log('Window loaded - Initializing gallery...');
    try {
        initializeImageModal();
        initializeGallery();

        const closeImageModal = document.getElementById('closeImageModal');
        const prevImage = document.getElementById('prevImage');
        const nextImage = document.getElementById('nextImage');

        if (!closeImageModal || !prevImage || !nextImage) {
            console.error('Modal controls not found!');
            return;
        }

        closeImageModal.addEventListener('click', () => hideModal(imageModal));
        prevImage.addEventListener('click', showPrevImage);
        nextImage.addEventListener('click', showNextImage);

        // Close modal when clicking outside the image
        imageModal.addEventListener('click', (e) => {
            if (e.target === imageModal) {
                hideModal(imageModal);
            }
        });

        // Keyboard navigation
        document.addEventListener('keydown', (e) => {
            if (!imageModal.classList.contains('hidden')) {
                if (e.key === 'ArrowLeft') {
                    showPrevImage();
                } else if (e.key === 'ArrowRight') {
                    showNextImage();
                } else if (e.key === 'Escape') {
                    hideModal(imageModal);
                }
            }
        });
        
        console.log('All event listeners attached successfully');
    } catch (error) {
        console.error('Error during initialization:', error);
    }
}); 