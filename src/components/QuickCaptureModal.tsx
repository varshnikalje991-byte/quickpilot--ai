import React, { useState, useEffect } from 'react';
import { 
  X, 
  Sparkles, 
  Mic, 
  MicOff, 
  Clipboard, 
  Check, 
  ArrowRight, 
  Clock, 
  Calendar,
  AlertCircle 
} from 'lucide-react';
import { parseNaturalLanguageInput, ParsedItemCandidate } from '../nlpEngine';

interface QuickCaptureModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSaveItems: (items: ParsedItemCandidate[]) => void;
}

export const QuickCaptureModal: React.FC<QuickCaptureModalProps> = ({
  isOpen,
  onClose,
  onSaveItems
}) => {
  const [inputText, setInputText] = useState('');
  const [parsedItems, setParsedItems] = useState<ParsedItemCandidate[] | null>(null);
  const [isListening, setIsListening] = useState(false);
  const [speechSupported, setSpeechSupported] = useState(false);

  useEffect(() => {
    // Check Web Speech API support
    const hasSpeech = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window;
    setSpeechSupported(hasSpeech);
  }, []);

  if (!isOpen) return null;

  const handleVoiceDictation = () => {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert('Speech recognition is not supported in this browser.');
      return;
    }

    if (isListening) {
      setIsListening(false);
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.lang = 'en-US';

      recognition.onstart = () => {
        setIsListening(true);
      };

      recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        if (transcript) {
          setInputText(prev => (prev ? `${prev} ${transcript}` : transcript));
          const parsed = parseNaturalLanguageInput(transcript);
          setParsedItems(parsed);
        }
        setIsListening(false);
      };

      recognition.onerror = () => {
        setIsListening(false);
      };

      recognition.onend = () => {
        setIsListening(false);
      };

      recognition.start();
    } catch (e) {
      setIsListening(false);
    }
  };

  const handleParse = () => {
    if (!inputText.trim()) return;
    const results = parseNaturalLanguageInput(inputText);
    setParsedItems(results);
  };

  const handleConfirmAndSave = () => {
    if (parsedItems && parsedItems.length > 0) {
      onSaveItems(parsedItems);
      setInputText('');
      setParsedItems(null);
      onClose();
    }
  };

  const samplePrompts = [
    "Remind me to finish physics assignment tomorrow at 6 PM, buy milk after college, and call Rahul on Sunday",
    "Study chemistry for 45 minutes tomorrow morning",
    "Remember that my Wi-Fi bill is ₹799",
    "Milk, whole wheat bread, eggs, apples"
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-slate-800">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-violet-primary/20 border border-violet-primary/30 flex items-center justify-center text-violet-primary">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">Quick Capture</h2>
              <p className="text-xs text-slate-400">Tell your device once. It organizes everything.</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body content */}
        <div className="p-5 overflow-y-auto space-y-4">
          <div className="relative">
            <textarea
              rows={4}
              value={inputText}
              onChange={e => {
                setInputText(e.target.value);
                if (parsedItems) setParsedItems(null);
              }}
              placeholder="e.g. Remind me to finish physics assignment tomorrow at 6 PM, buy milk after college, and call Rahul on Sunday..."
              className="w-full bg-slate-950/80 border border-slate-800 focus:border-violet-primary rounded-xl p-4 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-violet-primary transition-all resize-none"
            />

            {/* Voice Dictation button */}
            <button
              onClick={handleVoiceDictation}
              className={`absolute bottom-3.5 right-3.5 p-2 rounded-lg border transition-all ${
                isListening 
                  ? 'bg-rose-urgent text-white animate-pulse border-rose-urgent' 
                  : 'bg-slate-800 border-slate-700 text-slate-300 hover:text-white hover:bg-slate-700'
              }`}
              title={speechSupported ? 'Voice dictation' : 'Voice not supported in this browser'}
            >
              {isListening ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4 text-violet-light" />}
            </button>
          </div>

          {/* Quick paste & parse actions */}
          <div className="flex items-center justify-between">
            <button
              type="button"
              onClick={async () => {
                try {
                  const clip = await navigator.clipboard.readText();
                  if (clip) setInputText(clip);
                } catch {
                  // Fallback
                }
              }}
              className="flex items-center space-x-1.5 text-xs text-cyan-accent hover:underline font-medium"
            >
              <Clipboard className="w-3.5 h-3.5" />
              <span>Paste Clipboard</span>
            </button>

            <button
              onClick={handleParse}
              disabled={!inputText.trim()}
              className="flex items-center space-x-1.5 px-4 py-2 rounded-xl bg-violet-primary hover:bg-violet-light disabled:opacity-40 disabled:hover:bg-violet-primary text-white text-xs font-semibold shadow-md transition-all"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>Organize with AI</span>
            </button>
          </div>

          {/* Sample prompts */}
          {!parsedItems && (
            <div className="pt-2">
              <p className="text-xs text-slate-400 mb-2 font-medium">Try these sample thoughts:</p>
              <div className="flex flex-wrap gap-2">
                {samplePrompts.map((prompt, i) => (
                  <button
                    key={i}
                    onClick={() => {
                      setInputText(prompt);
                      const parsed = parseNaturalLanguageInput(prompt);
                      setParsedItems(parsed);
                    }}
                    className="text-left text-xs bg-slate-800/70 hover:bg-slate-800 border border-slate-700/60 rounded-lg px-2.5 py-1.5 text-slate-300 transition-colors"
                  >
                    "{prompt}"
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* AI Parsed Results Review */}
          {parsedItems && parsedItems.length > 0 && (
            <div className="pt-3 border-t border-slate-800 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <span className="w-2 h-2 rounded-full bg-emerald-success animate-ping" />
                  <span className="text-sm font-bold text-white">AI Extracted {parsedItems.length} Items</span>
                </div>
                <button 
                  onClick={() => setParsedItems(null)}
                  className="text-xs text-slate-400 hover:text-white"
                >
                  Edit Input
                </button>
              </div>

              <div className="space-y-2.5">
                {parsedItems.map((item, idx) => (
                  <div key={idx} className="p-3.5 rounded-xl bg-slate-950/70 border border-slate-800 space-y-1.5">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-2">
                        <span className="w-5 h-5 rounded-full bg-violet-primary/20 text-violet-primary text-xs font-bold flex items-center justify-center">
                          {idx + 1}
                        </span>
                        <span className="font-semibold text-sm text-slate-100">{item.title}</span>
                      </div>
                      <span className="text-xs px-2 py-0.5 rounded-md bg-cyan-accent/10 text-cyan-accent font-medium">
                        {item.type}
                      </span>
                    </div>

                    {item.clarificationQuestion && (
                      <div className="p-2 rounded-lg bg-amber-warning/10 border border-amber-warning/20 text-xs text-amber-200 flex items-start space-x-1.5">
                        <AlertCircle className="w-3.5 h-3.5 text-amber-warning shrink-0 mt-0.5" />
                        <span>{item.clarificationQuestion}</span>
                      </div>
                    )}

                    {item.checklist && item.checklist.length > 0 && (
                      <div className="text-xs text-slate-400 pl-7">
                        List items: {item.checklist.join(', ')}
                      </div>
                    )}

                    <div className="flex items-center space-x-2 text-[11px] text-slate-400 pl-7">
                      <span className="text-slate-300 font-medium">Category: {item.category}</span>
                      {item.dueDate && <span>• Due: {item.dueDate}</span>}
                      {item.timeString && <span>• Time: {item.timeString}</span>}
                    </div>
                  </div>
                ))}
              </div>

              <button
                onClick={handleConfirmAndSave}
                className="w-full flex items-center justify-center space-x-2 py-3 rounded-xl bg-emerald-success hover:bg-emerald-600 text-white font-semibold text-sm shadow-lg shadow-emerald-success/20 transition-all mt-4"
              >
                <Check className="w-4 h-4 stroke-[3]" />
                <span>Confirm & Save All {parsedItems.length} Items</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
