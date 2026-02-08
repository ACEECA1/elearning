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

    $('#backBtn').click(function() {
        alert("Retour au tableau de bord");
    });

    $('#selectVideoBtn').click(function() {
        $('#videoFileInput').click();
    });

    $('#videoFileInput').change(function() {
        var fichier = $(this).val();
        if (fichier != "") {
            alert("Vidéo sélectionnée : " + fichier);
            $('#videoUploadArea .upload-text').text("Fichier chargé : " + fichier);
        }
    });

    $('#videoUploadArea').hover(function() {
        $(this).css('background-color', '#f9fafb');
        $(this).css('border-color', '#4a3bbf');
    }, function() {
        $(this).css('background-color', '');
        $(this).css('border-color', '');
    });

    $('#selectPdfBtn').click(function() {
        $('#pdfFileInput').click();
    });

    $('#pdfFileInput').change(function() {
        var fichier = $(this).val();
        if (fichier != "") {
            alert("PDF sélectionné : " + fichier);
            $('#pdfUploadArea .upload-text').text("Fichier chargé : " + fichier);
        }
    });

    $('#pdfUploadArea').hover(function() {
        $(this).css('background-color', '#f9fafb');
        $(this).css('border-color', '#4a3bbf');
    }, function() {
        $(this).css('background-color', '');
        $(this).css('border-color', '');
    });

    $('#chapterForm').submit(function(e) {
        e.preventDefault();
        
        var titre = $('#chapterTitle').val();
        
        if (titre == "") {
            alert("Veuillez entrer un titre pour le chapitre.");
        } else {
            alert("Le chapitre '" + titre + "' et ses contenus ont été ajoutés avec succès.");
            $('#chapterTitle').val("");
            $('#chapterDescription').val("");
            $('.upload-text').text("Upload File");
        }
    });

    $('#cancelBtn').click(function() {
        var confirmation = confirm("Voulez-vous annuler l'ajout du chapitre ?");
        if (confirmation) {
            $('#chapterTitle').val("");
            $('#chapterDescription').val("");
        }
    });

    $('.sidebar-link').click(function() {
        $('.sidebar-link').removeClass('active');
        $(this).addClass('active');
    });

    console.log("Page d'ajout de contenu chargée");
});