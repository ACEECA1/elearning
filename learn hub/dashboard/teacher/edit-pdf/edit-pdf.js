$(document).ready(function() {

    var sidebar = $('#sidebar');
    
    $('#mobileSidebarToggle').click(function() {
        if (sidebar.is(':visible')) {
            sidebar.hide();
        } else {
            sidebar.show();
        }
    });

    $('#notificationBtn').click(function() {
        var btn = $(this);
        btn.animate({ top: "-5px" }, "fast");
        btn.animate({ top: "5px" }, "fast");
        btn.animate({ top: "0px" }, "fast");
    });

    $('.search-bar input').keypress(function(e) {
        if (e.which == 13) {
            var texte = $(this).val();
            if (texte != "") {
                alert("Recherche : " + texte);
            }
        }
    });

    $('#openModalBtn').click(function() {
        $('#readingModal').fadeIn();
    });

    $('#closeModalBtn').click(function() {
        $('#readingModal').fadeOut();
    });

    $('#selectPdfBtn').click(function() {
        $('#pdfFileInput').click();
    });

    $('#pdfFileInput').change(function() {
        var fichier = $(this).val();
        if (fichier != "") {
            alert("Fichier PDF sélectionné : " + fichier);
            $('.upload-title').text("Fichier chargé");
            $('.upload-subtitle').text(fichier);
        }
    });

    $('#uploadArea').hover(function() {
        $(this).css('background-color', '#f9fafb');
        $(this).css('border-color', '#4a3bbf');
    }, function() {
        $(this).css('background-color', '');
        $(this).css('border-color', '');
    });

    $('#readingMaterialForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#materialTitle').val();
        var fichier = $('#pdfFileInput').val();

        if (titre == "") {
            alert("Veuillez entrer un titre pour le document.");
        } else if (fichier == "") {
            alert("Veuillez sélectionner un fichier PDF.");
        } else {
            alert("Le document '" + titre + "' a été ajouté avec succès.");
            $('#readingModal').fadeOut();
            $('#materialTitle').val("");
            $('.upload-title').text("Upload PDF");
            $('.upload-subtitle').text("PDF only (Max 10MB)");
        }
    });

    $('.toolbar-icon-btn').click(function() {
        $(this).animate({ opacity: 0.5 }, 100, function() {
            $(this).animate({ opacity: 1 }, 100);
        });
    });

    $('.view-btn').click(function() {
        $('.view-btn').removeClass('active');
        $(this).addClass('active');
    });

    $('.btn-publish').click(function() {
        alert("Document publié !");
    });

    $('.btn-share').click(function() {
        alert("Lien de partage généré.");
    });

    console.log("Page d'édition PDF chargée");
});